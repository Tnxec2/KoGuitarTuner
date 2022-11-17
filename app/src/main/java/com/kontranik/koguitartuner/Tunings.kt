package com.kontranik.koguitartuner

val tunings = listOf(
    Tuning(
        desc = "Standard Tuning (EADGBE)",
        title = "Standard Tuning",
        notes = listOf(
            Note("E", 2, false),
            Note("A", 2, false),
            Note("D", 3, false),
            Note("G", 3, false),
            Note("B", 3, false),
            Note("E", 4, false),
        )
    ),
    Tuning(
        desc = "Drop D (DADGBE)",
        title = "Drop D",
        notes = listOf(
            Note("D", 2, false),
            Note("A", 2, false),
            Note("D", 3, false),
            Note("G", 3, false),
            Note("B", 3, false),
            Note("E", 4, false),
        )
    ),
    Tuning(
        desc = "Celtic Tuning (DADGAD)",
        title = "Celtic Tuning",
        notes = listOf(
            Note("D", 2, false),
            Note("A", 2, false),
            Note("D", 3, false),
            Note("G", 3, false),
            Note("A", 3, false),
            Note("D", 4, false),
        )
    ),
    Tuning(
        desc = "D Standard (DGCFAD)",
        title = "D Standard",
        notes = listOf(
            Note("D", 2, false),
            Note("G", 2, false),
            Note("C", 3, false),
            Note("F", 3, false),
            Note("A", 3, false),
            Note("D", 4, false),
        )
    ),
    Tuning(
        desc = "Open D (DADF#AD)",
        title = "Open D",
        notes = listOf(
            Note("D", 2, false),
            Note("A", 2, false),
            Note("D", 3, false),
            Note("F", 3, true),
            Note("A", 3, false),
            Note("D", 4, false),
        )
    ),
    Tuning(
        desc = "Open C (CGCGCE)",
        title = "Open C",
        notes = listOf(
            Note("C", 2, false),
            Note("G", 2, false),
            Note("C", 3, false),
            Note("G", 3, false),
            Note("C", 4, false),
            Note("E", 4, false),
        )
    ),
    Tuning(
        desc = "Drop C (CGCFAD)",
        title = "Drop C",
        notes = listOf(
            Note("C", 2, false),
            Note("G", 2, false),
            Note("C", 3, false),
            Note("F", 3, false),
            Note("A", 3, false),
            Note("D", 4, false),
        )
    )
)