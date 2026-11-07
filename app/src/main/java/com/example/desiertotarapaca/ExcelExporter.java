
package com.example.desiertotarapaca;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExcelExporter {

    private static void agregarArchivo(
            ZipOutputStream zip, String nombre, String contenido
    ) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String escaparXml(String texto) {
        if (texto == null) return "";

        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String crearHoja(
            SQLiteDatabase db,
            String tabla,
            String[] columnas,
            String[] encabezados,
            String orden,
            String limite
    ) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        xml.append("<sheetData>");

        int fila = 1;
        xml.append("<row r=\"").append(fila++).append("\">");

        for (String encabezado : encabezados) {
            xml.append("<c t=\"inlineStr\"><is><t>")
                    .append(escaparXml(encabezado))
                    .append("</t></is></c>");
        }

        xml.append("</row>");

        try (Cursor cursor = db.query(
                tabla, columnas, null, null,
                null, null, orden, limite
        )) {
            while (cursor.moveToNext()) {
                xml.append("<row r=\"").append(fila++).append("\">");

                for (int i = 0; i < columnas.length; i++) {
                    String valor = cursor.isNull(i)
                            ? ""
                            : cursor.getString(i);

                    xml.append("<c t=\"inlineStr\"><is><t>")
                            .append(escaparXml(valor))
                            .append("</t></is></c>");
                }

                xml.append("</row>");
            }
        }

        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    public static File generarExcel(Context context) throws IOException {

        DbHelper helper = new DbHelper(context);
        File carpeta = new File(context.getCacheDir(), "reportes");

        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de reportes");
        }

        File archivo = new File(carpeta, "Reporte_Desierto_Tarapaca.xlsx");

        try (SQLiteDatabase db = helper.getReadableDatabase();
             ZipOutputStream zip = new ZipOutputStream(
                     new FileOutputStream(archivo))) {

            agregarArchivo(zip, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                            "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                            "<Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                            "<Override PartName=\"/xl/worksheets/sheet3.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                            "<Override PartName=\"/xl/worksheets/sheet4.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                            "</Types>");

            agregarArchivo(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
                            "</Relationships>");

            agregarArchivo(zip, "xl/workbook.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
                            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                            "<sheets>" +
                            "<sheet name=\"Tareas\" sheetId=\"1\" r:id=\"rId1\"/>" +
                            "<sheet name=\"Servicios\" sheetId=\"2\" r:id=\"rId2\"/>" +
                            "<sheet name=\"Inventario\" sheetId=\"3\" r:id=\"rId3\"/>" +
                            "<sheet name=\"Clientes nuevos\" sheetId=\"4\" r:id=\"rId4\"/>" +
                            "</sheets></workbook>");

            StringBuilder relaciones = new StringBuilder();
            relaciones.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            relaciones.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");

            for (int i = 1; i <= 4; i++) {
                relaciones.append("<Relationship Id=\"rId")
                        .append(i)
                        .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                        .append(i)
                        .append(".xml\"/>");
            }

            relaciones.append("</Relationships>");

            agregarArchivo(zip, "xl/_rels/workbook.xml.rels",
                    relaciones.toString());

            agregarArchivo(zip, "xl/worksheets/sheet1.xml",
                    crearHoja(db, DbHelper.TABLA_TAREAS,
                            new String[]{
                                    DbHelper.COL_TAREA_ID,
                                    DbHelper.COL_TAREA_CLIENTE,
                                    DbHelper.COL_TAREA_TIPO,
                                    DbHelper.COL_TAREA_HORA,
                                    DbHelper.COL_TAREA_FECHA,
                                    DbHelper.COL_TAREA_ESTADO
                            },
                            new String[]{
                                    "ID", "Cliente", "Tipo",
                                    "Hora", "Fecha", "Estado"
                            }, null, null));

            agregarArchivo(zip, "xl/worksheets/sheet2.xml",
                    crearHoja(db, DbHelper.TABLA_SERVICIOS,
                            new String[]{
                                    DbHelper.COL_ID,
                                    DbHelper.COL_OT,
                                    DbHelper.COL_CLIENTE,
                                    DbHelper.COL_TIPO,
                                    DbHelper.COL_DESCRIPCION,
                                    DbHelper.COL_FECHA,
                                    DbHelper.COL_FIRMADO,
                                    DbHelper.COL_SINCRONIZADO
                            },
                            new String[]{
                                    "ID", "OT", "Cliente", "Tipo",
                                    "Descripción", "Fecha",
                                    "Firmado", "Sincronizado"
                            }, null, null));

            agregarArchivo(zip, "xl/worksheets/sheet3.xml",
                    crearHoja(db, DbHelper.TABLA_INVENTARIO,
                            new String[]{
                                    DbHelper.COL_QUIM_ID,
                                    DbHelper.COL_QUIM_NOMBRE,
                                    DbHelper.COL_QUIM_TIPO,
                                    DbHelper.COL_QUIM_STOCK,
                                    DbHelper.COL_QUIM_UNIDAD
                            },
                            new String[]{
                                    "ID", "Nombre", "Tipo",
                                    "Stock", "Unidad"
                            }, null, null));

            agregarArchivo(zip, "xl/worksheets/sheet4.xml",
                    crearHoja(db, DbHelper.TABLA_CLIENTES,
                            new String[]{
                                    DbHelper.COL_CLI_ID,
                                    DbHelper.COL_CLI_NOMBRE,
                                    DbHelper.COL_CLI_RUT,
                                    DbHelper.COL_CLI_FAENA,
                                    DbHelper.COL_CLI_TELEFONO
                            },
                            new String[]{
                                    "ID", "Nombre", "RUT",
                                    "Faena", "Teléfono"
                            },
                            DbHelper.COL_CLI_ID + " DESC", "5"));
        }

        return archivo;
    }
}
