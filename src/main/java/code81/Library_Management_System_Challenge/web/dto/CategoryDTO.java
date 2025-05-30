package code81.Library_Management_System_Challenge.web.dto;

public class CategoryDTO {
    private String name;
    private String description;
    private Long parentId; // nullable if null this means, it's a root category

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
