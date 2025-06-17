package com.final_app.tools;

import javafx.scene.image.Image;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.*;
import java.net.URL;

public class SVGUtil {

    public static Image loadSVG(String resourcePath, float width, float height, String colorHex) {
        try {
            // Load resource URL
            URL url = SVGUtil.class.getResource(resourcePath);
            if (url == null) {
                throw new IllegalArgumentException("Resource niet gevonden: " + resourcePath);
            }

            // Read SVG content as String
            StringBuilder svgContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    svgContent.append(line).append("\n");
                }
            }

            // Replace fills and/or strokes (basic version)
            String modifiedSvg = svgContent.toString()
                    .replaceAll("fill=\"#[A-Fa-f0-9]{3,6}\"", "fill=\"" + colorHex + "\"")
                    .replaceAll("stroke=\"#[A-Fa-f0-9]{3,6}\"", "stroke=\"" + colorHex + "\"");

            // Transcode modified SVG to PNG
            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);

            // Use string as input
            ByteArrayInputStream svgStream = new ByteArrayInputStream(modifiedSvg.getBytes());
            TranscoderInput input = new TranscoderInput(svgStream);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);

            transcoder.transcode(input, output);

            ByteArrayInputStream bis = new ByteArrayInputStream(outputStream.toByteArray());
            return new Image(bis);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Image loadSVG(String resourcePath, float width, float height) {
        try {
            // Haal de resource URL op
            URL url = SVGUtil.class.getResource(resourcePath);
            if (url == null) {
                throw new IllegalArgumentException("Resource niet gevonden: " + resourcePath);
            }
            // Converteer naar een externe vorm (bijv. "jar:file:...")
            String uri = url.toExternalForm();

            // Configureer de transcoder
            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);

            // Gebruik de URI-string in plaats van een FileInputStream
            TranscoderInput input = new TranscoderInput(uri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);

            transcoder.transcode(input, output);

            ByteArrayInputStream bis = new ByteArrayInputStream(outputStream.toByteArray());
            return new Image(bis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
