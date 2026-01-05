package com.demo.controller;

import com.demo.payload.request.ApiDocInfo;
import com.demo.payload.request.GenerateDocRequest;
import com.demo.service.WordDocService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/docs")
public class ApiDocController {

    private final WordDocService wordDocService;
    public ApiDocController(WordDocService wordDocService) {
        this.wordDocService = wordDocService;
    }

    @GetMapping("/apis")
    public List<Object> listApis() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Object apiDocs = restTemplate.getForObject("http://localhost:8088/v3/api-docs", Object.class);

            // Trả nguyên JSON OpenAPI cho client xử lý hoặc generate doc
            return List.of(apiDocs);

        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi lấy API docs: " + ex.getMessage(), ex);
        }
    }


    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody GenerateDocRequest req) throws Exception {
        byte[] file = wordDocService.generateDoc(req.apis());

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=api-docs.docx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .body(file);
    }

}

