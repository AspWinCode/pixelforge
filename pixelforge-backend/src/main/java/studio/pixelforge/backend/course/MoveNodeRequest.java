package studio.pixelforge.backend.course;

// parentId=null — переместить в корень курса (тип узла должен быть MODULE).
public record MoveNodeRequest(Long parentId, Integer sortOrder) {
}
