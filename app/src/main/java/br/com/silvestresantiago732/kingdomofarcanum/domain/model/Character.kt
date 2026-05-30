package br.com.silvestresantiago732.kingdomofarcanum.domain.model

data class Character(
    val id: String = "",
    val name: String = "",
    val race: String = "",
    val characterClass: String = "",
    val observation: String = "",
    val lore: String = "",
    val imageUrl: String = "",
    val level: Int = 1,
    val currentHp: Int = 10,
    val maxHp: Int = 10,
    val currentMana: Int = 5,
    val maxMana: Int = 5,
    val currentXp: Int = 0,
    val maxXp: Int = 100,
    val intelligence: Int = 0,
    val strength: Int = 0,
    val agility: Int = 0,
    val gold: Int = 0,
    val attributePoints: Int = 0,
    val skills: List<Skill> = emptyList(),
    val items: List<Item> = emptyList()
)
