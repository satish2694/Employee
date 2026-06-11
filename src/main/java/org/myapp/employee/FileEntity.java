package org.myapp.employee;

import jakarta.persistence.*;

@Entity
@Table(name = "FILE_STORE")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String fileName;
    private String contentType;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] data;

    public FileEntity() {}

    public FileEntity(String fileName, String contentType, byte[] data) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
    }

    // Getters
    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public byte[] getData() { return data; }

    // Setters
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setData(byte[] data) { this.data = data; }
}
