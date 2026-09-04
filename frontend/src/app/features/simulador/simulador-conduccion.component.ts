import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EscenarioService } from '../escenarios/escenario.service';
import { InformeIA, MetricasConduccion, SimulacionService } from '../practicas/simulacion.service';

type EstadoJuego = 'listo' | 'corriendo' | 'pausado' | 'finalizado';

interface VehiculoNpc {
  carril: number;
  y: number;
  velocidad: number;
  color: string;
}

interface SemaforoEvento {
  y: number;
  fase: 'verde' | 'rojo';
  timer: number;
  resuelto: 'pendiente' | 'respetado' | 'ignorado';
}

@Component({
  selector: 'app-simulador-conduccion',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './simulador-conduccion.component.html',
  styleUrl: './simulador-conduccion.component.css'
})
export class SimuladorConduccionComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('lienzo', { static: true }) lienzo!: ElementRef<HTMLCanvasElement>;

  // Configuración de la pista (unidades lógicas) y puntajes (Etapa 3)
  readonly ANCHO = 480;
  readonly ALTO = 640;
  readonly MARGEN = 40;
  readonly CARRILES = 3;
  readonly LIMITE_KMH = 60;
  readonly VEL_MAX_KMH = 120;
  readonly PENAL_COLISION = 20;
  readonly PENAL_SEMAFORO = 15;
  readonly PENAL_SALIDA = 10;
  readonly PENAL_DISTANCIA = 8;
  readonly PENAL_EXCESO = 5;

  estado: EstadoJuego = 'listo';
  velocidadKmh = 0;
  velocidadMax = 0;
  segundos = 0;
  distanciaKm = 0;

  // Infracciones detectadas (Etapa 3)
  colisiones = 0;
  salidasCarril = 0;
  excesosVelocidad = 0;
  semaforosIgnorados = 0;
  distanciaInsegura = 0;
  semaforosRespetados = 0;

  // Integración con el backend (Etapa 3)
  idEscenario: number | null = null;
  nombreEscenario = 'Conducción libre';
  idSimulacionBackend: number | null = null;
  modoLocal = false;
  aviso = '';
  guardando = false;
  puntajeServidor: number | null = null;
  informe: InformeIA | null = null;

  private ctx!: CanvasRenderingContext2D;
  private raf = 0;
  private ultimaMarca = 0;
  private teclas = new Set<string>();
  private xAuto = 0;
  private desplazamiento = 0;
  private npcs: VehiculoNpc[] = [];
  private semaforos: SemaforoEvento[] = [];
  private tiempoSpawn = 0;
  private tiempoSpawnSemaforo = 8;
  private enExceso = false;
  private fueraVia = false;
  private colisionCooldown = 0;
  private destello = 0;
  private tailTimer = 0;
  private sumaVel = 0;
  private onKeyDown = (e: KeyboardEvent) => this.manejarTecla(e, true);
  private onKeyUp = (e: KeyboardEvent) => this.manejarTecla(e, false);
  private onResize = () => this.ajustarLienzo();

  private readonly COLORES_NPC = ['#e0442e', '#f2b705', '#7b61ff', '#00a8b5', '#ff7b00'];
  private readonly Y_AUTO = this.ALTO - 120;

  constructor(
    private route: ActivatedRoute,
    private escenarioService: EscenarioService,
    private simulacionService: SimulacionService
  ) {}

  ngOnInit(): void {
    const param = Number(this.route.snapshot.queryParamMap.get('escenario'));
    if (Number.isInteger(param) && param > 0) {
      this.idEscenario = param;
      this.escenarioService.buscarPorId(param).subscribe({
        next: (e) => { this.nombreEscenario = e.nombre; },
        error: () => { /* se mantiene el nombre genérico */ }
      });
    } else {
      this.escenarioService.listar(0, 1).subscribe({
        next: (resp) => {
          const primero = resp?.content?.[0];
          if (primero?.id) {
            this.idEscenario = primero.id;
            this.nombreEscenario = primero.nombre ?? this.nombreEscenario;
          }
        },
        error: () => { /* modo libre sin escenario */ }
      });
    }
  }

  ngAfterViewInit(): void {
    const canvas = this.lienzo.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    this.ctx = ctx;
    this.xAuto = this.centroCarril(1);
    this.ajustarLienzo();
    window.addEventListener('keydown', this.onKeyDown);
    window.addEventListener('keyup', this.onKeyUp);
    window.addEventListener('resize', this.onResize);
    this.ultimaMarca = performance.now();
    this.bucle(this.ultimaMarca);
  }

  ngOnDestroy(): void {
    cancelAnimationFrame(this.raf);
    window.removeEventListener('keydown', this.onKeyDown);
    window.removeEventListener('keyup', this.onKeyUp);
    window.removeEventListener('resize', this.onResize);
  }

  // ─── Controles de la simulación ───
  iniciar(): void {
    if (this.estado === 'finalizado') this.reiniciar();
    if (this.idSimulacionBackend === null && this.idEscenario !== null && !this.modoLocal) {
      this.simulacionService.iniciar(this.idEscenario).subscribe({
        next: (s) => { this.idSimulacionBackend = s.idSimulacion; },
        error: () => {
          this.modoLocal = true;
          this.aviso = 'Sin conexión con el servidor: se juega en modo local y no se guardará el resultado.';
        }
      });
    }
    if (this.idEscenario === null) this.modoLocal = true;
    this.estado = 'corriendo';
    this.ultimaMarca = performance.now();
  }

  pausar(): void {
    if (this.estado === 'corriendo') this.estado = 'pausado';
  }

  reanudar(): void {
    if (this.estado === 'pausado') {
      this.estado = 'corriendo';
      this.ultimaMarca = performance.now();
    }
  }

  reiniciar(): void {
    this.estado = 'listo';
    this.velocidadKmh = 0;
    this.velocidadMax = 0;
    this.segundos = 0;
    this.distanciaKm = 0;
    this.colisiones = 0;
    this.salidasCarril = 0;
    this.excesosVelocidad = 0;
    this.semaforosIgnorados = 0;
    this.distanciaInsegura = 0;
    this.semaforosRespetados = 0;
    this.puntajeServidor = null;
    this.informe = null;
    this.idSimulacionBackend = null;
    this.modoLocal = this.idEscenario === null;
    this.aviso = this.modoLocal ? 'Sin escenario disponible: modo local.' : '';
    this.guardando = false;
    this.xAuto = this.centroCarril(1);
    this.desplazamiento = 0;
    this.npcs = [];
    this.semaforos = [];
    this.tiempoSpawn = 0;
    this.tiempoSpawnSemaforo = 8;
    this.enExceso = false;
    this.fueraVia = false;
    this.colisionCooldown = 0;
    this.destello = 0;
    this.tailTimer = 0;
    this.sumaVel = 0;
    this.teclas.clear();
  }

  finalizar(): void {
    if (this.estado !== 'corriendo' && this.estado !== 'pausado') return;
    this.estado = 'finalizado';
    this.velocidadKmh = 0;
    if (this.idSimulacionBackend !== null && !this.modoLocal) {
      this.guardando = true;
      this.simulacionService.finalizarConduccion(this.idSimulacionBackend, this.metricas()).subscribe({
        next: (r) => {
          this.puntajeServidor = Number(r.simulacion.puntajeFinal);
          this.informe = r.retroalimentacion;
          this.guardando = false;
        },
        error: () => {
          this.guardando = false;
          this.aviso = 'No se pudo guardar en el servidor; se conserva el resumen local.';
        }
      });
    }
  }

  get tiempo(): string {
    const total = Math.floor(this.segundos);
    const min = Math.floor(total / 60).toString().padStart(2, '0');
    const seg = (total % 60).toString().padStart(2, '0');
    return `${min}:${seg}`;
  }

  get puntaje(): number {
    const descuento = this.colisiones * this.PENAL_COLISION
      + this.semaforosIgnorados * this.PENAL_SEMAFORO
      + this.salidasCarril * this.PENAL_SALIDA
      + this.distanciaInsegura * this.PENAL_DISTANCIA
      + this.excesosVelocidad * this.PENAL_EXCESO;
    return Math.max(0, 100 - descuento);
  }

  get velocidadPromedio(): number {
    return this.segundos > 0 ? this.sumaVel / this.segundos : 0;
  }

  get tailgating(): boolean {
    return this.tailTimer > 0.3;
  }

  private metricas(): MetricasConduccion {
    return {
      duracionSegundos: Math.max(1, Math.floor(this.segundos)),
      velocidadPromedio: Math.round(this.velocidadPromedio * 100) / 100,
      velocidadMaxima: Math.round(this.velocidadMax * 100) / 100,
      excesosVelocidad: this.excesosVelocidad,
      colisiones: this.colisiones,
      salidasCarril: this.salidasCarril,
      semaforosIgnorados: this.semaforosIgnorados,
      distanciaInsegura: this.distanciaInsegura,
      semaforosRespetados: this.semaforosRespetados
    };
  }

  // ─── Entrada por teclado ───
  private manejarTecla(e: KeyboardEvent, presionada: boolean): void {
    const tecla = e.key.toLowerCase();
    const juego = ['arrowup', 'arrowdown', 'arrowleft', 'arrowright', 'w', 'a', 's', 'd', ' '];
    if (juego.includes(tecla) && (this.estado === 'corriendo' || this.estado === 'pausado')) {
      e.preventDefault();
    }
    if (tecla === 'p' && presionada && (this.estado === 'corriendo' || this.estado === 'pausado')) {
      this.estado === 'corriendo' ? this.pausar() : this.reanudar();
      return;
    }
    if (presionada) this.teclas.add(tecla);
    else this.teclas.delete(tecla);
  }

  // ─── Bucle principal ───
  private bucle(marca: number): void {
    const dt = Math.min((marca - this.ultimaMarca) / 1000, 0.1);
    this.ultimaMarca = marca;
    if (this.estado === 'corriendo') this.actualizar(dt);
    this.dibujar();
    this.raf = requestAnimationFrame((m) => this.bucle(m));
  }

  private actualizar(dt: number): void {
    const arriba = this.teclas.has('arrowup') || this.teclas.has('w');
    const abajo = this.teclas.has('arrowdown') || this.teclas.has('s');
    const izq = this.teclas.has('arrowleft') || this.teclas.has('a');
    const der = this.teclas.has('arrowright') || this.teclas.has('d');

    const sobreCesped = this.xAuto - 22 < this.MARGEN || this.xAuto + 22 > this.ANCHO - this.MARGEN;
    if (arriba) this.velocidadKmh += (sobreCesped ? 15 : 35) * dt;
    else if (abajo) this.velocidadKmh -= 55 * dt;
    else this.velocidadKmh -= 6 * dt;
    if (sobreCesped && this.velocidadKmh > 40) this.velocidadKmh = Math.max(40, this.velocidadKmh - 40 * dt);
    this.velocidadKmh = Math.max(0, Math.min(this.VEL_MAX_KMH, this.velocidadKmh));

    if (izq) this.xAuto -= 240 * dt;
    if (der) this.xAuto += 240 * dt;
    this.xAuto = Math.max(12, Math.min(this.ANCHO - 12, this.xAuto));

    this.segundos += dt;
    this.sumaVel += this.velocidadKmh * dt;
    this.distanciaKm += (this.velocidadKmh / 3600) * dt;
    this.velocidadMax = Math.max(this.velocidadMax, this.velocidadKmh);
    this.desplazamiento = (this.desplazamiento + this.velocidadKmh * 3 * dt) % 64;
    if (this.colisionCooldown > 0) this.colisionCooldown -= dt;
    if (this.destello > 0) this.destello -= dt;

    // Exceso de velocidad (episodio con histéresis)
    if (!this.enExceso && this.velocidadKmh > this.LIMITE_KMH) {
      this.enExceso = true;
      this.excesosVelocidad++;
    } else if (this.enExceso && this.velocidadKmh < this.LIMITE_KMH - 5) {
      this.enExceso = false;
    }

    // Salida del carril / calzada
    if (!this.fueraVia && sobreCesped && this.velocidadKmh > 5) {
      this.fueraVia = true;
      this.salidasCarril++;
    } else if (this.fueraVia && !sobreCesped) {
      this.fueraVia = false;
    }

    // Tráfico ambiental
    this.tiempoSpawn -= dt;
    if (this.tiempoSpawn <= 0 && this.npcs.length < 5) {
      this.tiempoSpawn = 1.4 + Math.random() * 1.6;
      const carril = Math.floor(Math.random() * this.CARRILES);
      const ocupado = this.npcs.some((n) => n.carril === carril && n.y < 60);
      if (!ocupado) {
        this.npcs.push({
          carril,
          y: -90,
          velocidad: 40 + Math.random() * 30,
          color: this.COLORES_NPC[Math.floor(Math.random() * this.COLORES_NPC.length)]
        });
      }
    }
    const factor = 3;
    this.npcs.forEach((n) => { n.y += (this.velocidadKmh - n.velocidad) * factor * dt; });
    this.npcs = this.npcs.filter((n) => n.y < this.ALTO + 120 && n.y > -220);

    // Colisiones (AABB) con 2s de invulnerabilidad
    if (this.colisionCooldown <= 0) {
      const golpe = this.npcs.find((n) =>
        Math.abs(this.centroCarril(n.carril) - this.xAuto) < 44 && Math.abs(n.y - this.Y_AUTO) < 76);
      if (golpe) {
        this.colisiones++;
        this.colisionCooldown = 2;
        this.destello = 0.6;
        this.velocidadKmh *= 0.35;
        this.npcs = this.npcs.filter((n) => n !== golpe);
      }
    }

    // Distancia segura: mismo carril, vehículo delante a menos de 110px y más lento
    const miCarril = this.carrilCercano(this.xAuto);
    const delante = this.npcs
      .filter((n) => n.carril === miCarril && n.y < this.Y_AUTO)
      .map((n) => (this.Y_AUTO - 38) - (n.y + 38))
      .filter((gap) => gap >= 0 && gap < 110);
    const masLento = delante.length > 0 && this.npcs.some((n) =>
      n.carril === miCarril && n.y < this.Y_AUTO
      && (this.Y_AUTO - 38) - (n.y + 38) >= 0
      && (this.Y_AUTO - 38) - (n.y + 38) < 110 && this.velocidadKmh > n.velocidad);
    if (masLento) {
      this.tailTimer += dt;
      if (this.tailTimer >= 2) {
        this.distanciaInsegura++;
        this.tailTimer = 0;
      }
    } else {
      this.tailTimer = 0;
    }

    // Semáforos como eventos que descienden con el mundo
    this.tiempoSpawnSemaforo -= dt;
    if (this.tiempoSpawnSemaforo <= 0 && !this.semaforos.some((s) => s.resuelto === 'pendiente')) {
      this.tiempoSpawnSemaforo = 20 + Math.random() * 8;
      this.semaforos.push({ y: -80, fase: 'verde', timer: 0, resuelto: 'pendiente' });
    }
    const frente = this.Y_AUTO - 38;
    this.semaforos.forEach((s) => {
      const previa = s.y;
      s.y += this.velocidadKmh * factor * dt;
      if (s.resuelto !== 'pendiente') return;
      s.timer += dt;
      if (s.fase === 'verde' && s.timer >= 5) { s.fase = 'rojo'; s.timer = 0; }
      if (s.fase === 'rojo' && this.velocidadKmh <= 5 && s.y >= frente - 140 && s.y < frente) {
        s.resuelto = 'respetado';
        s.fase = 'verde';
        this.semaforosRespetados++;
      } else if (s.fase === 'rojo' && previa < frente && s.y >= frente && this.velocidadKmh > 5) {
        s.resuelto = 'ignorado';
        s.fase = 'verde';
        this.semaforosIgnorados++;
      }
    });
    this.semaforos = this.semaforos.filter((s) => s.y < this.ALTO + 80);
  }

  // ─── Dibujo ───
  private ajustarLienzo(): void {
    const canvas = this.lienzo.nativeElement;
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = this.ANCHO * dpr;
    canvas.height = this.ALTO * dpr;
    this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  private centroCarril(i: number): number {
    const anchoVia = this.ANCHO - this.MARGEN * 2;
    return this.MARGEN + (anchoVia / this.CARRILES) * (i + 0.5);
  }

  private carrilCercano(x: number): number {
    let mejor = 0;
    let mejorDist = Math.abs(x - this.centroCarril(0));
    for (let i = 1; i < this.CARRILES; i++) {
      const d = Math.abs(x - this.centroCarril(i));
      if (d < mejorDist) { mejorDist = d; mejor = i; }
    }
    return mejor;
  }

  private dibujar(): void {
    const c = this.ctx;
    c.fillStyle = '#3d8b4f';
    c.fillRect(0, 0, this.ANCHO, this.ALTO);
    c.fillStyle = '#3c4043';
    c.fillRect(this.MARGEN, 0, this.ANCHO - this.MARGEN * 2, this.ALTO);
    c.fillStyle = '#f1f3f4';
    c.fillRect(this.MARGEN - 5, 0, 5, this.ALTO);
    c.fillRect(this.ANCHO - this.MARGEN, 0, 5, this.ALTO);
    const anchoVia = this.ANCHO - this.MARGEN * 2;
    c.fillStyle = '#fdd663';
    for (let i = 1; i < this.CARRILES; i++) {
      const x = this.MARGEN + (anchoVia / this.CARRILES) * i;
      for (let y = -64; y < this.ALTO + 64; y += 64) {
        c.fillRect(x - 3, y + this.desplazamiento, 6, 32);
      }
    }
    this.dibujarSenalVelocidad(c);
    this.semaforos.forEach((s) => this.dibujarSemaforoEn(c, s.y, s.fase));
    this.npcs.forEach((n) => this.dibujarAuto(c, this.centroCarril(n.carril), n.y, n.color));
    if (this.destello <= 0 || Math.floor(this.destello * 10) % 2 === 0) {
      this.dibujarAuto(c, this.xAuto, this.Y_AUTO, '#1a73e8', true);
    }
    if (this.tailgating && this.estado === 'corriendo') {
      c.fillStyle = '#fbbc04';
      c.font = '700 15px system-ui, sans-serif';
      c.textAlign = 'center';
      c.fillText('⚠ Distancia insegura', this.ANCHO / 2, 30);
    }

    if (this.estado === 'listo' || this.estado === 'pausado') {
      c.fillStyle = 'rgba(10, 25, 45, 0.55)';
      c.fillRect(0, 0, this.ANCHO, this.ALTO);
      c.fillStyle = '#fff';
      c.font = '700 26px system-ui, sans-serif';
      c.textAlign = 'center';
      c.fillText(this.estado === 'listo' ? 'Listo para conducir' : 'En pausa', this.ANCHO / 2, this.ALTO / 2);
      c.font = '400 15px system-ui, sans-serif';
      c.fillText(
        this.estado === 'listo' ? 'Pulsa Iniciar y usa ← → ↑ ↓ o W A S D' : 'Pulsa Reanudar o la tecla P',
        this.ANCHO / 2, this.ALTO / 2 + 30
      );
    }
  }

  private dibujarAuto(c: CanvasRenderingContext2D, x: number, y: number, color: string, jugador = false): void {
    const w = 44, h = 76;
    c.fillStyle = 'rgba(0,0,0,0.25)';
    c.fillRect(x - w / 2 + 3, y - h / 2 + 5, w, h);
    c.fillStyle = color;
    c.beginPath();
    c.roundRect(x - w / 2, y - h / 2, w, h, 10);
    c.fill();
    c.fillStyle = '#0f2537';
    c.beginPath();
    c.roundRect(x - w / 2 + 7, y - h / 2 + 16, w - 14, 20, 5);
    c.fill();
    c.fillStyle = '#cfe3fc';
    c.fillRect(x - w / 2 + 5, y + h / 2 - 10, 10, 5);
    c.fillRect(x + w / 2 - 15, y + h / 2 - 10, 10, 5);
    if (jugador) {
      c.strokeStyle = '#fff';
      c.lineWidth = 2;
      c.beginPath();
      c.roundRect(x - w / 2, y - h / 2, w, h, 10);
      c.stroke();
    }
  }

  private dibujarSenalVelocidad(c: CanvasRenderingContext2D): void {
    const x = 20, y = 300;
    c.fillStyle = '#5f6368';
    c.fillRect(x + 8, y + 22, 4, 40);
    c.fillStyle = '#fff';
    c.beginPath();
    c.arc(x + 10, y, 24, 0, Math.PI * 2);
    c.fill();
    c.strokeStyle = '#d93025';
    c.lineWidth = 5;
    c.beginPath();
    c.arc(x + 10, y, 24, 0, Math.PI * 2);
    c.stroke();
    c.fillStyle = '#202124';
    c.font = '700 20px system-ui, sans-serif';
    c.textAlign = 'center';
    c.fillText(String(this.LIMITE_KMH), x + 10, y + 7);
  }

  private dibujarSemaforoEn(c: CanvasRenderingContext2D, y: number, fase: 'verde' | 'rojo'): void {
    const x = this.ANCHO - 20;
    // Línea de pare sobre la calzada
    c.fillStyle = 'rgba(255,255,255,0.85)';
    c.fillRect(this.MARGEN + 6, y - 3, this.ANCHO - this.MARGEN * 2 - 12, 6);
    c.fillStyle = '#5f6368';
    c.fillRect(x - 12, y - 70, 4, 70);
    c.fillStyle = '#202124';
    c.beginPath();
    c.roundRect(x - 32, y - 116, 30, 52, 6);
    c.fill();
    const luces: Array<'rojo' | 'verde'> = ['rojo', 'verde'];
    luces.forEach((luz, i) => {
      c.fillStyle = fase === luz ? (luz === 'rojo' ? '#ea4335' : '#34a853') : '#3c4043';
      c.beginPath();
      c.arc(x - 17, y - 104 + i * 22, 7, 0, Math.PI * 2);
      c.fill();
    });
  }
}
