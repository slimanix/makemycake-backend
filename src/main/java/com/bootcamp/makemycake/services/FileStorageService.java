package com.bootcamp.makemycake.services;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads/")
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Could not create the directory where the uploaded files will be stored.",
                    ex
            );
        }
    }

    /**
     * Stocke un fichier dans le système
     * @param file Fichier à stocker
     * @return Le nom unique du fichier stocké
     * @throws IOException Si le stockage échoue
     */
    public String storeFile(MultipartFile file) throws IOException {
        // Normalise le nom du fichier
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Vérifie les injections de chemin
        if (originalFileName.contains("..")) {
            throw new IOException(
                    "Sorry! Filename contains invalid path sequence " + originalFileName
            );
        }

        // Génère un nom unique
        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

        // Copie le fichier vers la cible
        Files.copy(
                file.getInputStream(),
                targetLocation,
                StandardCopyOption.REPLACE_EXISTING
        );

        return uniqueFileName;
    }

    /**
     * Charge un fichier depuis le système
     * @param fileName Nom du fichier à charger
     * @return Le fichier sous forme de Resource
     * @throws IOException Si le fichier n'existe pas
     */
    public Path loadFile(String fileName) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        if (!Files.exists(filePath)) {
            throw new IOException("File not found " + fileName);
        }
        return filePath;
    }

    /**
     * Supprime un fichier
     * @param fileName Nom du fichier à supprimer
     * @throws IOException Si la suppression échoue
     */
    public void deleteFile(String fileName) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Files.deleteIfExists(filePath);
    }
}