package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "historial_acceso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_acceso")
    private Integer idHistorialAcceso;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private Instant fechaHora = Instant.now();

    @Column(name = "direccion_ip")
    private String direccionIp;

    @Column(name = "dispositivo", length = 255)
    private String dispositivo;

    @Column(name = "navegador", length = 255)
    private String navegador;

    @Column(name = "acceso_exitoso", nullable = false)
    private boolean accesoExitoso;

    @Column(name = "detalle", length = 255)
    private String detalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}
