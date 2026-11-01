package com.datagram.datagramweb.api;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

public final class Uploads {
  private Uploads() {}

  public static String image(MultipartFile f) throws java.io.IOException {
    byte[] b = f.getBytes();
    String type;
    if (b.length > 2 * 1024 * 1024 || b.length < 12)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem deve ter até 2 MB");
    if (b[0] == (byte) 0xff && b[1] == (byte) 0xd8 && b[2] == (byte) 0xff) type = "image/jpeg";
    else if (Arrays.equals(
        Arrays.copyOf(b, 8), new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10}))
      type = "image/png";
    else if (new String(b, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
        && new String(b, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP"))
      type = "image/webp";
    else
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use uma imagem PNG, JPEG ou WebP");
    return "data:" + type + ";base64," + Base64.getEncoder().encodeToString(b);
  }

  public static String pdf(MultipartFile f) throws java.io.IOException {
    byte[] b = f.getBytes();
    if (b.length < 5
        || b.length > 5 * 1024 * 1024
        || !new String(b, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use um PDF de até 5 MB");
    return "data:application/pdf;base64," + Base64.getEncoder().encodeToString(b);
  }
}
