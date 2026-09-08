package com.sbvia.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sbvia.backend.entity.BitacoraAuditoria;
import com.sbvia.backend.repository.BitacoraAuditoriaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final BitacoraAuditoriaRepository repository;

    public List<BitacoraAuditoria> obtenerAuditoria(String tabla, String operacion, String usuario, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Specification<BitacoraAuditoria> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tabla != null && !tabla.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("nombreTabla"), tabla));
            }
            if (operacion != null && !operacion.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("operacion"), operacion));
            }
            if (usuario != null && !usuario.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("usuarioApp")), "%" + usuario.toLowerCase() + "%"));
            }
            if (fechaInicio != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaHora"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaHora"), fechaFin));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, Sort.by(Sort.Direction.DESC, "fechaHora"));
    }

    public byte[] generarReportePdf(String tabla, String operacion, String usuario, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<BitacoraAuditoria> registros = obtenerAuditoria(tabla, operacion, usuario, fechaInicio, fechaFin);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("SBVIA - Reporte de Auditoría de Base de Datos", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20f);
            document.add(titulo);
            
            // Subtítulo con filtros
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10);
            document.add(new Paragraph("Filtros aplicados:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            document.add(new Paragraph("Tabla: " + (tabla != null ? tabla : "Todas") + 
                                       " | Operación: " + (operacion != null ? operacion : "Todas") + 
                                       " | Usuario: " + (usuario != null ? usuario : "Todos"), fontSub));
            document.add(new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), fontSub));
            document.add(new Paragraph("\n"));

            // Tabla
            PdfPTable pdfTable = new PdfPTable(6);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{1.5f, 2f, 1.5f, 2.5f, 3f, 3f});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            String[] headers = {"Fecha", "Tabla", "Operación", "Usuario", "Datos Antiguos", "Datos Nuevos"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
                cell.setPadding(5);
                pdfTable.addCell(cell);
            }

            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (BitacoraAuditoria log : registros) {
                pdfTable.addCell(new Phrase(log.getFechaHora() != null ? log.getFechaHora().format(dtf) : "", rowFont));
                pdfTable.addCell(new Phrase(log.getNombreTabla() != null ? log.getNombreTabla() : "", rowFont));
                pdfTable.addCell(new Phrase(log.getOperacion() != null ? log.getOperacion() : "", rowFont));
                pdfTable.addCell(new Phrase(log.getUsuarioApp() != null ? log.getUsuarioApp() : (log.getUsuarioDb() != null ? log.getUsuarioDb() : ""), rowFont));
                
                String ant = log.getDatosAnteriores() != null ? log.getDatosAnteriores() : "-";
                String nue = log.getDatosNuevos() != null ? log.getDatosNuevos() : "-";
                // Truncar si es muy largo para el PDF
                if(ant.length() > 200) ant = ant.substring(0, 197) + "...";
                if(nue.length() > 200) nue = nue.substring(0, 197) + "...";

                pdfTable.addCell(new Phrase(ant, rowFont));
                pdfTable.addCell(new Phrase(nue, rowFont));
            }

            document.add(pdfTable);
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte PDF", e);
        }
    }
}
