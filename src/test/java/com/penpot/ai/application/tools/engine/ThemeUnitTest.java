package com.penpot.ai.application.tools.engine;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ThemeUnitTest {

    @Test
    @DisplayName("Factory: Création manuelle de thèmes solid et gradient")
    void testFactories() {
        Theme solid = Theme.solid("Custom", "#FF0000", "#FFF", "#000", "#000", "#FFF", "#000", "#000");
        assertThat(solid.isGradient()).isFalse();
        assertThat(solid.bgSolid).isEqualTo("#FF0000");

        Theme grad = Theme.gradient("Grad", "#000", "#FFF", "#FFF", "#000", "#000", "#FFF", "#000", "#000");
        assertThat(grad.isGradient()).isTrue();
        assertThat(grad.g1).isEqualTo("#000");
    }
}