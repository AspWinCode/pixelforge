package studio.pixelforge.backend.course;

// Дерево курса — ровно 3 уровня. Требуемый тип родителя фиксирован:
// MODULE — только под курсом (parent = null), TOPIC — только под MODULE,
// SUBTOPIC — только под TOPIC. Глубже некуда, см. CourseNodeService.
public enum CourseNodeType {
    MODULE,
    TOPIC,
    SUBTOPIC;

    // Тип родителя, обязательный для этого типа узла, или null, если узел
    // должен быть без родителя (корень дерева).
    public CourseNodeType requiredParentType() {
        return switch (this) {
            case MODULE -> null;
            case TOPIC -> MODULE;
            case SUBTOPIC -> TOPIC;
        };
    }
}
