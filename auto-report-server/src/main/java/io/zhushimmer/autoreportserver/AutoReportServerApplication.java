package io.zhushimmer.autoreportserver;

import io.zhushimmer.autoreportserver.utils.Command;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@SpringBootApplication
public class AutoReportServerApplication {
    public static void main(String[] args) {
//        initPythonEnv();
//        resourceExtractor();

        SpringApplication.run(AutoReportServerApplication.class, args);
    }

    private static void resourceExtractor() {
        Path dirPath = Paths.get("scripts");

        try {
            if (!Files.isDirectory(dirPath)) {
                Files.createDirectory(dirPath);
            }
        } catch (Exception e) {}

        try {
            String[] fileNames = {"database_tools.py"};
            for (String fileName : fileNames) {
                InputStream inputStream = new ClassPathResource("scripts/" + fileName).getInputStream();
                Path targetPath = Paths.get("scripts", fileName);
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {}
    }

    public static void initPythonEnv() {
        String venvDir = ".venv";
        File venvFolder = new File(venvDir);
        if (!venvFolder.exists()) {
            System.out.println("[!] The virtual environment `.venv` does not exist, Creating...");

            try {
                // 使用 python -m venv .venv 创建虚拟环境
                int exitCode = Command.run(new ProcessBuilder("python", "-m", "venv", venvDir));
                if (exitCode == 0) {
                    System.out.println("[+] Virtual environment created successfully!");
                    Command.run(new ProcessBuilder(".venv/Scripts/pip", "install", "pandas", "numpy"));
                } else {
                    System.err.println("[-] Virtual environment creation failed, exit code: " + exitCode);
                }
            } catch (Exception e) {
                System.err.println("Error creating virtual environment: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
