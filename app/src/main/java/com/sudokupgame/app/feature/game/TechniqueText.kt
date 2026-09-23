package com.sudokupgame.app.feature.game

import androidx.annotation.StringRes
import com.sudokupgame.app.R
import com.sudokupgame.app.data.GameMode
import com.sudokupgame.engine.Technique

@get:StringRes
val Technique.nameRes: Int
    get() = when (this) {
        Technique.NAKED_SINGLE -> R.string.technique_naked_single
        Technique.HIDDEN_SINGLE -> R.string.technique_hidden_single
        Technique.NAKED_PAIR -> R.string.technique_naked_pair
        Technique.HIDDEN_PAIR -> R.string.technique_hidden_pair
        Technique.POINTING -> R.string.technique_pointing
        Technique.NAKED_TRIPLE -> R.string.technique_naked_triple
        Technique.HIDDEN_TRIPLE -> R.string.technique_hidden_triple
        Technique.BOX_LINE_REDUCTION -> R.string.technique_box_line_reduction
        Technique.X_WING -> R.string.technique_x_wing
        Technique.XY_WING -> R.string.technique_xy_wing
        Technique.SWORDFISH -> R.string.technique_swordfish
    }

@StringRes
fun Technique.descriptionRes(mode: GameMode): Int =
    if (mode == GameMode.ANIMAL) animalDescriptionRes else descriptionRes

@get:StringRes
private val Technique.animalDescriptionRes: Int
    get() = when (this) {
        Technique.NAKED_SINGLE -> R.string.technique_naked_single_desc_animal
        Technique.HIDDEN_SINGLE -> R.string.technique_hidden_single_desc_animal
        Technique.NAKED_PAIR -> R.string.technique_naked_pair_desc_animal
        Technique.HIDDEN_PAIR -> R.string.technique_hidden_pair_desc_animal
        Technique.POINTING -> R.string.technique_pointing_desc_animal
        Technique.NAKED_TRIPLE -> R.string.technique_naked_triple_desc_animal
        Technique.HIDDEN_TRIPLE -> R.string.technique_hidden_triple_desc_animal
        Technique.BOX_LINE_REDUCTION -> R.string.technique_box_line_reduction_desc_animal
        Technique.X_WING -> R.string.technique_x_wing_desc_animal
        Technique.XY_WING -> R.string.technique_xy_wing_desc_animal
        Technique.SWORDFISH -> R.string.technique_swordfish_desc_animal
    }

@get:StringRes
private val Technique.descriptionRes: Int
    get() = when (this) {
        Technique.NAKED_SINGLE -> R.string.technique_naked_single_desc
        Technique.HIDDEN_SINGLE -> R.string.technique_hidden_single_desc
        Technique.NAKED_PAIR -> R.string.technique_naked_pair_desc
        Technique.HIDDEN_PAIR -> R.string.technique_hidden_pair_desc
        Technique.POINTING -> R.string.technique_pointing_desc
        Technique.NAKED_TRIPLE -> R.string.technique_naked_triple_desc
        Technique.HIDDEN_TRIPLE -> R.string.technique_hidden_triple_desc
        Technique.BOX_LINE_REDUCTION -> R.string.technique_box_line_reduction_desc
        Technique.X_WING -> R.string.technique_x_wing_desc
        Technique.XY_WING -> R.string.technique_xy_wing_desc
        Technique.SWORDFISH -> R.string.technique_swordfish_desc
    }
