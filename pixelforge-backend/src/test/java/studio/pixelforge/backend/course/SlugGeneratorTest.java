package studio.pixelforge.backend.course;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlugGeneratorTest {

    @Test
    void transliteratesCyrillicToKebabCase() {
        assertEquals("tsikly-v-snap", SlugGenerator.slugify("Циклы в Snap!"));
    }

    @Test
    void collapsesPunctuationAndTrimsDashes() {
        assertEquals("hello-world", SlugGenerator.slugify("  Hello, World!!  "));
    }

    @Test
    void fallsBackToCourseWhenTitleHasNoLatinizableChars() {
        assertEquals("course", SlugGenerator.uniqueSlug("!!!", s -> false));
    }

    @Test
    void appendsNumericSuffixOnCollision() {
        Set<String> taken = Set.of("intro", "intro-2");
        assertEquals("intro-3", SlugGenerator.uniqueSlug("Intro", taken::contains));
    }

    @Test
    void firstCandidateWinsWhenFree() {
        assertEquals("intro", SlugGenerator.uniqueSlug("Intro", s -> false));
    }

    @Test
    void slugIsAlwaysLowercaseAsciiKebab() {
        String slug = SlugGenerator.slugify("Тест 123 — модуль №1");
        assertTrue(slug.matches("[a-z0-9-]*"));
    }
}
