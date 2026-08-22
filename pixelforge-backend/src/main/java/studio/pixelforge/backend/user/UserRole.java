package studio.pixelforge.backend.user;

public enum UserRole {
    STUDENT,
    METHODIST,  // создаёт контент: задания, лекции
    TRAINER,    // работает с учениками: проверяет сдачи, видит статистику
    PARENT,
    SCHOOL_ADMIN
}
