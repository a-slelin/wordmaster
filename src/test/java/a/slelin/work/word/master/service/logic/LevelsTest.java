package a.slelin.work.word.master.service.logic;

import a.slelin.work.word.master.entity.Grade;
import a.slelin.work.word.master.entity.TrainingMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelsTest {

    @Test
    void levelBoundaries() {
        assertEquals(1, Levels.level(0));
        assertEquals(1, Levels.level(99));
        assertEquals(2, Levels.level(100));
        assertEquals(2, Levels.level(299));
        assertEquals(3, Levels.level(300));
        assertEquals(10, Levels.level(4500));
    }

    @Test
    void progressInsideLevel() {
        assertEquals(50, Levels.levelXp(350));
        assertEquals(300, Levels.levelXpGoal(350));
    }

    @Test
    void hardModesGiveBonus() {
        assertTrue(Levels.answerXp(Grade.GOOD, TrainingMode.TYPING) > Levels.answerXp(Grade.GOOD, TrainingMode.FLASHCARDS));
        assertEquals(Levels.answerXp(Grade.AGAIN, TrainingMode.TYPING), Levels.answerXp(Grade.AGAIN, TrainingMode.FLASHCARDS));
    }
}
