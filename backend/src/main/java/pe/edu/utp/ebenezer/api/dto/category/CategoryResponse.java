package pe.edu.utp.ebenezer.api.dto.category;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Boolean active
) {
}
