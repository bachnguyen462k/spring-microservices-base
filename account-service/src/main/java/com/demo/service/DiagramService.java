package com.demo.service;

import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.stereotype.Service;
import java.io.*;

@Service
public class DiagramService {
    public byte[] generate(String uml) throws IOException {
        SourceStringReader reader = new SourceStringReader(uml);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        reader.generateImage(out);
        return out.toByteArray();
    }
}

