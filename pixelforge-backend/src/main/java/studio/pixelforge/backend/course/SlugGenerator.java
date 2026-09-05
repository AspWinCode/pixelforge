package studio.pixelforge.backend.course;

import java.util.Map;
import java.util.function.Predicate;

// Внутренний slug курса (не публичный URL, см. спеку студии §7.4) —
// автогенерится из title транслитерацией + kebab-case, при коллизии
// добавляется числовой суффикс.
final class SlugGenerator {

    private static final Map<Character, String> TRANSLIT = Map.ofEntries(
        Map.entry('а', "a"), Map.entry('б', "b"), Map.entry('в', "v"), Map.entry('г', "g"),
        Map.entry('д', "d"), Map.entry('е', "e"), Map.entry('ё', "e"), Map.entry('ж', "zh"),
        Map.entry('з', "z"), Map.entry('и', "i"), Map.entry('й', "y"), Map.entry('к', "k"),
        Map.entry('л', "l"), Map.entry('м', "m"), Map.entry('н', "n"), Map.entry('о', "o"),
        Map.entry('п', "p"), Map.entry('р', "r"), Map.entry('с', "s"), Map.entry('т', "t"),
        Map.entry('у', "u"), Map.entry('ф', "f"), Map.entry('х', "h"), Map.entry('ц', "ts"),
        Map.entry('ч', "ch"), Map.entry('ш', "sh"), Map.entry('щ', "sch"), Map.entry('ъ', ""),
        Map.entry('ы', "y"), Map.entry('ь', ""), Map.entry('э', "e"), Map.entry('ю', "yu"),
        Map.entry('я', "ya")
    );

    private SlugGenerator() {
    }

    static String slugify(String title) {
        if (title == null) {
            return "";
        }
        StringBuilder transliterated = new StringBuilder();
        for (char c : title.toLowerCase().toCharArray()) {
            String mapped = TRANSLIT.get(c);
            transliterated.append(mapped != null ? mapped : c);
        }
        String slug = transliterated.toString()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
        return slug;
    }

    // Подбирает первый свободный вариант: "slug", "slug-2", "slug-3", ...
    // (taken уже учитывает исключение текущего id при апдейте — см. вызов).
    static String uniqueSlug(String title, Predicate<String> taken) {
        String base = slugify(title);
        if (base.isEmpty()) {
            base = "course";
        }
        String candidate = base;
        int suffix = 2;
        while (taken.test(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}
