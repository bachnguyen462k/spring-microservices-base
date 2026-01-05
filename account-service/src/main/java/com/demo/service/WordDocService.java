package com.demo.service;

import com.demo.payload.request.ApiDocInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class WordDocService {

    private final DiagramService diagramService;
    private JsonNode openApiRoot; // lưu root JSON OpenAPI nội bộ

    public WordDocService(DiagramService diagramService) {
        this.diagramService = diagramService;
    }

    public byte[] generateDoc(List<ApiDocInfo> apis) throws Exception {
        // 1) Load OpenAPI JSON nội bộ (offline mạng nội bộ)
        String openApiJson = new RestTemplate().getForObject("http://localhost:8088/v3/api-docs", String.class);
        openApiRoot = new ObjectMapper().readTree(openApiJson);
        JsonNode paths = openApiRoot.get("paths");

        // 2) Load template Word
        InputStream template = getClass().getResourceAsStream("/templates/api-template.docx");
        XWPFDocument doc = new XWPFDocument(template);

        for (ApiDocInfo api : apis) {
            JsonNode methodNode = paths.path(api.endpoint().toLowerCase()).path(api.method().toLowerCase());
            if (methodNode.isMissingNode()) continue;

            // Tạo bảng Request
            XWPFTable tableReq = doc.createTable();
            createHeader(tableReq);
            fillSchema(tableReq, methodNode.path("requestBody").path("content").path("application/json").path("schema"));

            // Tạo bảng Response 200
            XWPFTable tableRes = doc.createTable();
            createHeader(tableRes);
            fillSchema(tableRes, methodNode.path("responses").path("200").path("content").path("*/*").path("schema"));

            // Sinh Sequence Diagram
            String uml = buildUml(api);
            byte[] diagram = diagramService.generate(uml);
            replaceImage(doc, "{{SEQUENCE_DIAGRAM}}", diagram);

            // Replace text trong template
            replaceText(doc, "{{ENDPOINT}}", api.endpoint());
            replaceText(doc, "{{METHOD}}", api.method());
            replaceText(doc, "{{SUMMARY}}", api.summary());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.write(out);
        return out.toByteArray();
    }

    private void createHeader(XWPFTable table) {
        XWPFTableRow row = table.getRow(0);
        row.getCell(0).setText("Field");
        row.addNewTableCell().setText("Type");
        row.addNewTableCell().setText("Description");
    }

    // 3) Fix hàm fillSchema
    private void fillSchema(XWPFTable table, JsonNode schemaNode) {
        if (schemaNode == null || schemaNode.isMissingNode()) return;

        // Nếu schema dùng $ref
        if (schemaNode.has("$ref")) {
            String ref = schemaNode.get("$ref").asText();
            String schemaName = ref.substring(ref.lastIndexOf("/") + 1);

            JsonNode props = openApiRoot.path("components").path("schemas").path(schemaName).path("properties");
            if (props.isMissingNode()) return;

            props.fields().forEachRemaining(entry -> {
                String field = entry.getKey();
                JsonNode p = entry.getValue();
                XWPFTableRow row = table.createRow();

                row.getCell(0).setText(field);
                row.getCell(1).setText(p.path("type").asText("object"));
                row.getCell(2).setText(p.path("description").asText(""));
            });
        }
        // Nếu schema inline có properties trực tiếp
        else if (schemaNode.has("properties")) {
            JsonNode props = schemaNode.get("properties");
            props.fields().forEachRemaining(entry -> {
                String field = entry.getKey();
                JsonNode p = entry.getValue();
                XWPFTableRow row = table.createRow();

                row.getCell(0).setText(field);
                row.getCell(1).setText(p.path("type").asText("object"));
                row.getCell(2).setText(p.path("description").asText(""));
            });
        }
    }

    private void replaceText(XWPFDocument doc, String find, String replace) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            if (p.getText().contains(find)) {
                for (XWPFRun r : p.getRuns()) {
                    if (r.getText(0) != null) {
                        r.setText(r.getText(0).replace(find, replace), 0);
                    }
                }
            }
        }
    }

    private void replaceImage(XWPFDocument doc, String placeholder, byte[] imageBytes) throws Exception {
        for (XWPFParagraph p : doc.getParagraphs()) {
            if (p.getText().contains(placeholder)) {
                for (XWPFRun r : p.getRuns()) {
                    if (r.getText(0) != null && r.getText(0).contains(placeholder)) {
                        r.setText("", 0);
                        r.addPicture(
                                new ByteArrayInputStream(imageBytes),
                                Document.PICTURE_TYPE_PNG,
                                "sequence.png",
                                Units.toEMU(450),
                                Units.toEMU(250)
                        );
                    }
                }
            }
        }
    }

    private String buildUml(ApiDocInfo api) {
        return """
        @startuml
        actor Client
        Client -> Backend: Gọi %s %s
        Backend --> Client: Trả response 200 OK
        @enduml
        """.formatted(api.method(), api.endpoint());
    }
}
