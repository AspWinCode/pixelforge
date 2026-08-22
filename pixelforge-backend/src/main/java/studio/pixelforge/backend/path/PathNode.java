package studio.pixelforge.backend.path;

// type: "LECTURE" | "ASSIGNMENT". targetId — id лекции или задания
// соответственно, фронтенд сам решает, куда вести по клику.
public record PathNode(
    String type,
    Long targetId,
    String title,
    boolean completed,
    boolean locked
) {}
