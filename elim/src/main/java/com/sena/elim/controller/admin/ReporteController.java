package com.sena.elim.controller.admin;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // mapeo para mostrar la vista de reportes
    @GetMapping("")
    public String mostrarReportes(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");
        if (usuario == null || usuario.get("id_rol_fk") == null || Integer.parseInt(usuario.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        // cargar las categorias de los filtroas dde inventario
        List<Map<String, Object>> categorias = jdbcTemplate.queryForList("SELECT id_categoria, nombre_categoria FROM categorias ORDER BY nombre_categoria");
        model.addAttribute("categorias", categorias);

        return "admin/reportes";
    }

    // 1.reporte pdff de inventario
    @GetMapping("/pdf/inventario")
    public void descargarPdfInventario(@RequestParam(value = "id_categoria", required = false) Integer idCategoria,
                                       @RequestParam(value = "estado", required = false) String estado,
                                       HttpServletResponse response,
                                       HttpSession session) throws IOException {

        validarAdmin(session);

        StringBuilder sql = new StringBuilder("SELECT p.id_producto, p.nombre_producto, c.nombre_categoria, p.precio, p.stock, p.estado " +
                "FROM productos p LEFT JOIN categorias c ON p.id_categoria_fk = c.id_categoria WHERE 1=1");

        if (idCategoria != null) {
            sql.append(" AND p.id_categoria_fk = ").append(idCategoria);
        }
        if (estado != null && !estado.isEmpty()) {
            sql.append(" AND p.estado = '").append(estado).append("'");
        }

        List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql.toString());

        configurarRespuestaPDF(response, "reporte_inventario.pdf");

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            agregarCabeceraPDF(document, "Reporte de Inventario - Elim Pijamas");

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3f, 2f, 2f, 1.5f, 2f});
            table.setSpacingBefore(10f);

            agregarCeldaCabecera(table, "ID");
            agregarCeldaCabecera(table, "Producto");
            agregarCeldaCabecera(table, "Categoría");
            agregarCeldaCabecera(table, "Precio");
            agregarCeldaCabecera(table, "Stock");
            agregarCeldaCabecera(table, "Estado");

            for (Map<String, Object> row : resultados) {
                agregarCeldaTabla(table, getSafeString(row.get("id_producto")));
                agregarCeldaTabla(table, getSafeString(row.get("nombre_producto")));
                agregarCeldaTabla(table, getSafeString(row.get("nombre_categoria")));
                agregarCeldaTabla(table, "$" + getSafeString(row.get("precio")));
                agregarCeldaTabla(table, getSafeString(row.get("stock")));
                agregarCeldaTabla(table, getSafeString(row.get("estado")));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Error al generar el PDF de inventario", e);
        }
    }

    // 2.reporte pdff de pedioss
    @GetMapping("/pdf/pedidos")
    public void descargarPdfPedidos(@RequestParam(value = "estado_pedido", required = false) String estadoPedido,
                                    HttpServletResponse response,
                                    HttpSession session) throws IOException {
        validarAdmin(session);

        String sqlQuery = "SELECT p.id_pedido, p.fecha_pedido, p.total_pago, p.estado_pedido, " +
                "u.nombres, u.apellidos " +
                "FROM pedidos p " +
                "LEFT JOIN usuarios u ON p.documento_usuario_fk = u.documento WHERE 1=1";

        if (estadoPedido != null && !estadoPedido.isEmpty()) {
            sqlQuery += " AND p.estado_pedido = '" + estadoPedido + "'";
        }

        List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sqlQuery);

        configurarRespuestaPDF(response, "reporte_pedidos.pdf");

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            agregarCabeceraPDF(document, "Reporte de Pedidos - Elim Pijamas");

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3.5f, 2.5f, 2f, 2f});
            table.setSpacingBefore(10f);

            agregarCeldaCabecera(table, "ID");
            agregarCeldaCabecera(table, "Cliente");
            agregarCeldaCabecera(table, "Fecha");
            agregarCeldaCabecera(table, "Total");
            agregarCeldaCabecera(table, "Estado");

            for (Map<String, Object> row : resultados) {
                String cliente = getSafeString(row.get("nombres")) + " " + getSafeString(row.get("apellidos"));
                agregarCeldaTabla(table, getSafeString(row.get("id_pedido")));
                agregarCeldaTabla(table, cliente);
                agregarCeldaTabla(table, getSafeString(row.get("fecha_pedido")));
                agregarCeldaTabla(table, "$" + getSafeString(row.get("total_pago")));
                agregarCeldaTabla(table, getSafeString(row.get("estado_pedido")));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Error al generar el PDF de pedidos", e);
        }
    }

    // 3.reporte pdf devoluciones
    @GetMapping("/pdf/devoluciones")
    public void descargarPdfDevoluciones(HttpServletResponse response,
                                         HttpSession session) throws IOException {
        validarAdmin(session);

        String sqlQuery = "SELECT d.id_devolucion, d.id_pedido_fk, d.motivo, d.fecha_devolucion, d.estado_devolucion, u.nombres, u.apellidos " +
                "FROM devoluciones d " +
                "LEFT JOIN pedidos p ON d.id_pedido_fk = p.id_pedido " +
                "LEFT JOIN usuarios u ON p.documento_usuario_fk = u.documento";

        List<Map<String, Object>> resultados;
        try {
            resultados = jdbcTemplate.queryForList(sqlQuery);
        } catch (Exception e) {
            resultados = new ArrayList<>();
        }

        configurarRespuestaPDF(response, "reporte_devoluciones.pdf");

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            agregarCabeceraPDF(document, "Reporte de Devoluciones - Elim Pijamas");

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 1.5f, 3f, 3f, 2.5f, 2f});
            table.setSpacingBefore(10f);

            agregarCeldaCabecera(table, "ID Dev");
            agregarCeldaCabecera(table, "ID Ped");
            agregarCeldaCabecera(table, "Cliente");
            agregarCeldaCabecera(table, "Motivo");
            agregarCeldaCabecera(table, "Fecha");
            agregarCeldaCabecera(table, "Estado");

            for (Map<String, Object> row : resultados) {
                String cliente = getSafeString(row.get("nombres")) + " " + getSafeString(row.get("apellidos"));
                agregarCeldaTabla(table, getSafeString(row.get("id_devolucion")));
                agregarCeldaTabla(table, getSafeString(row.get("id_pedido_fk")));
                agregarCeldaTabla(table, cliente);
                agregarCeldaTabla(table, getSafeString(row.get("motivo")));
                agregarCeldaTabla(table, getSafeString(row.get("fecha_devolucion")));
                agregarCeldaTabla(table, getSafeString(row.get("estado_devolucion")));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Error al generar el PDF de devoluciones", e);
        }
    }

    // metodos auxiliares
    private void validarAdmin(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");
        if (usuario == null || usuario.get("id_rol_fk") == null || Integer.parseInt(usuario.get("id_rol_fk").toString()) != 1) {
            throw new RuntimeException("Acceso no autorizado");
        }
    }

    private void configurarRespuestaPDF(HttpServletResponse response, String nombreArchivo) {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo);
    }

    private void agregarCabeceraPDF(Document document, String tituloTexto) throws DocumentException {
        Paragraph titulo = new Paragraph(tituloTexto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY));
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);
    }

    private void agregarCeldaCabecera(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        cell.setBackgroundColor(new Color(219, 112, 147)); // Tono acorde al diseño rosa de la marca
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void agregarCeldaTabla(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "", FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK)));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String getSafeString(Object val) {
        return val != null ? val.toString() : "";
    }
}