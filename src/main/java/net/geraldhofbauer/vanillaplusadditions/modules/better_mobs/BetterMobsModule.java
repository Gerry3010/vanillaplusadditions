package net.geraldhofbauer.vanillaplusadditions.modules.better_mobs;

import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.geraldhofbauer.vanillaplusadditions.modules.better_mobs.config.BetterMobsConfig;
import net.geraldhofbauer.vanillaplusadditions.modules.better_mobs.config.BetterMobsConfigKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BetterMobsModule extends AbstractModule<BetterMobsModule, BetterMobsConfig> {
    public BetterMobsModule() {
        super("better_mobs",
                "Better Mobs",
                "Enhances mob equipment and effects for a more engaging gameplay experience.",
                BetterMobsConfig::new);
    }

    @Override
    protected void onInitialize() {
        // Event-Handler registrieren
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    protected void onCommonSetup() {
        if (getConfig().shouldDebugLog()) {
            getLogger().debug("Better Mobs module common setup complete");
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!isModuleEnabled()) {
            return;
        }
        if (!(event.getEntity() instanceof Monster mob) || !(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        BetterMobsConfig config = getConfig();
        String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        if (!config.getEnabledMobs().contains(mobId)) {
            return;
        }

        // StringBuilder für Debug-Nachricht
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("Mob spawned with properties:\n");
        debugInfo.append("Type: ").append(mobId).append("\n");
        debugInfo.append("Position: ").append(mob.blockPosition()).append("\n");

        // Zufälliges Equipment-Setup basierend auf Y-Koordinate
        int y = mob.blockPosition().getY();
        UUID uuid = mob.getUUID();
        var setup = config.getRandomEquipmentSetupForMob(serverLevel.dimension(), uuid, y);
        getLogger().debug("Setup: {}", setup);
        if (setup == null) {
            if (getConfig().shouldDebugLog()) {
                getLogger().debug("No equipment setup found for mob '{}' at Y: {}", mobId, y);
            }
            return;
        }

        // Materialtyp bestimmen
        var materials = setup.get(BetterMobsConfigKey.GEAR_TYPES);
        if (materials.isEmpty()) {
            if (getConfig().shouldDebugLog()) {
                getLogger().debug("No gear type found for mob at Y: {}", y);
            }
            return;
        }
        String material = materials.get(new Random(uuid.getLeastSignificantBits()).nextInt(materials.size()));
        debugInfo.append("Material: ").append(material).append("\n");

        if (getConfig().shouldDebugLog()) {
            getLogger().debug("Applying gear type '{}' to mob '{}' at Y: {}", material, mobId, y);
        }

        // Armor nur für Mobs aus enabledMobsWithArmor
        if (config.getEnabledMobsWithArmor().contains(mobId)) {
            // Hole die Liste der Rüstungsteile, die spawnen sollen
            List<String> spawnedArmor = setup.get(BetterMobsConfigKey.ARMOR_CHANCES);

            // Erstelle und setze die Rüstungsteile basierend auf den Spawn-Chancen
            if (spawnedArmor.contains("helmet")) {
                ItemStack helmet = getItemForTypeAndMaterial("helmet", material);
                if (helmet != null && !helmet.isEmpty()) {
                    // Durability setzen
                    int maxDurability = helmet.getMaxDamage();
                    int percentDurability = config.getMaxDurabilityValue();
                    int percentDropChance = config.getDropChanceValue();
                    helmet.setDamageValue(maxDurability - (maxDurability * percentDurability / 100));
                    // Drop-Chance setzen
                    mob.setDropChance(EquipmentSlot.HEAD, percentDropChance / 100.0f);
                    mob.setItemSlot(EquipmentSlot.HEAD, helmet);
                    debugInfo.append("Armor - Helmet: ").append(helmet.getDisplayName()).append("\n");
                    // Enchantments für Helmet
                    applyArmorEnchantments(serverLevel,
                            uuid,
                            helmet,
                            setup.get(BetterMobsConfigKey.HELMET_ENCHANTMENTS),
                            setup.get(BetterMobsConfigKey.ENCHANTMENT_LEVELS));
                    if (helmet.isEnchanted()) {
                        debugInfo.append("Helmet Enchantments: ").append(helmet.getTagEnchantments()).append("\n");
                    }
                }
            }

            if (spawnedArmor.contains("chestplate")) {
                ItemStack chest = getItemForTypeAndMaterial("chestplate", material);
                if (chest != null && !chest.isEmpty()) {
                    int maxDurability = chest.getMaxDamage();
                    int percentDurability = config.getMaxDurabilityValue();
                    int percentDropChance = config.getDropChanceValue();
                    chest.setDamageValue(maxDurability - (maxDurability * percentDurability / 100));
                    mob.setDropChance(EquipmentSlot.CHEST, percentDropChance / 100.0f);
                    mob.setItemSlot(EquipmentSlot.CHEST, chest);
                    debugInfo.append("Armor - Chestplate: ").append(chest.getDisplayName()).append("\n");
                    // Enchantments für Chestplate
                    applyArmorEnchantments(serverLevel,
                            uuid,
                            chest,
                            setup.get(BetterMobsConfigKey.CHESTPLATE_ENCHANTMENTS),
                            setup.get(BetterMobsConfigKey.ENCHANTMENT_LEVELS));
                    if (chest.isEnchanted()) {
                        debugInfo.append("Chestplate Enchantments: ").append(chest.getTagEnchantments()).append("\n");
                    }
                }
            }

            if (spawnedArmor.contains("leggings")) {
                ItemStack legs = getItemForTypeAndMaterial("leggings", material);
                if (legs != null && !legs.isEmpty()) {
                    int maxDurability = legs.getMaxDamage();
                    int percentDurability = config.getMaxDurabilityValue();
                    int percentDropChance = config.getDropChanceValue();
                    legs.setDamageValue(maxDurability - (maxDurability * percentDurability / 100));
                    mob.setDropChance(EquipmentSlot.LEGS, percentDropChance / 100.0f);
                    mob.setItemSlot(EquipmentSlot.LEGS, legs);
                    debugInfo.append("Armor - Leggings: ").append(legs.getDisplayName()).append("\n");
                    // Enchantments für Leggings
                    applyArmorEnchantments(serverLevel,
                            uuid,
                            legs,
                            setup.get(BetterMobsConfigKey.LEGGINGS_ENCHANTMENTS),
                            setup.get(BetterMobsConfigKey.ENCHANTMENT_LEVELS));
                    if (legs.isEnchanted()) {
                        debugInfo.append("Leggings Enchantments: ").append(legs.getTagEnchantments()).append("\n");
                    }
                }
            }

            if (spawnedArmor.contains("boots")) {
                ItemStack boots = getItemForTypeAndMaterial("boots", material);
                if (boots != null && !boots.isEmpty()) {
                    int maxDurability = boots.getMaxDamage();
                    int percentDurability = config.getMaxDurabilityValue();
                    int percentDropChance = config.getDropChanceValue();
                    boots.setDamageValue(maxDurability - (maxDurability * percentDurability / 100));
                    mob.setDropChance(EquipmentSlot.FEET, percentDropChance / 100.0f);
                    mob.setItemSlot(EquipmentSlot.FEET, boots);
                    debugInfo.append("Armor - Boots: ").append(boots.getDisplayName()).append("\n");
                    // Enchantments für Boots
                    applyArmorEnchantments(serverLevel,
                            uuid,
                            boots,
                            setup.get(BetterMobsConfigKey.BOOTS_ENCHANTMENTS),
                            setup.get(BetterMobsConfigKey.ENCHANTMENT_LEVELS));
                    if (boots.isEnchanted()) {
                        debugInfo.append("Boots Enchantments: ").append(boots.getTagEnchantments()).append("\n");
                    }
                }
            }
        }

        // Potion Effects
        var effects = setup.get(BetterMobsConfigKey.POTION_EFFECTS);
        if (effects != null && !effects.isEmpty()) {
            debugInfo.append("Potion Effects:\n");
            effects.forEach((effect) -> {
                final int level = new Random(uuid.getLeastSignificantBits()).nextInt(1, 2);
                final int duration = Integer.MAX_VALUE;
                debugInfo.append("- ").append(effect).append(" (Level ").append(level).append(")\n");
                switch (effect) {
                    case "speed" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, level - 1));
                    case "strength" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, level - 1));
                    case "regeneration" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, level - 1));
                    case "fire_resistance" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, level - 1));
                    case "invisibility" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, level - 1));
                    case "water_breathing" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration, level - 1));
                    case "night_vision" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration, level - 1));
                    case "jump_boost" -> mob.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, level - 1));
                    case "weakness" -> mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, level - 1));
                    case "slowness" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, level - 1));
                    case "mining_fatigue" ->
                            mob.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, level - 1));
                    case "poison" -> mob.addEffect(new MobEffectInstance(MobEffects.POISON, duration, level - 1));
                    case "wither" -> mob.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, level - 1));
                    case null, default -> {
                    }
                }
            });
        }

        // Debug-Nachricht ausgeben und an alle Spieler senden, wenn debug aktiviert ist
        if (getConfig().shouldDebugLog()) {
            getLogger().debug(debugInfo.toString());

            // Erstelle eine kompakte Nachricht mit Hover-Text
            var hoverComponent = Component.literal(debugInfo.toString());
            var mainMessage = Component
                    .literal("§6[Debug] §rMob mit besonderen Eigenschaften gespawnt! §7(Hover für Details)")
                    .withStyle(style -> style
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    hoverComponent
                            ))
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/tp @s %d %d %d".formatted(mob.blockPosition().getX(),
                                            mob.blockPosition().getY(),
                                            mob.blockPosition().getZ())
                            ))
                    );

            // Sende die Nachricht an alle Spieler auf dem Server
            serverLevel.getServer().getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(mainMessage)
            );
        }
    }

    // Hilfsfunktion: Gibt das passende ItemStack für Typ und Material zurück
    private ItemStack getItemForTypeAndMaterial(String type, String material) {
        return switch (type) {
            case "helmet" -> switch (material) {
                case "gold" -> new ItemStack(Items.GOLDEN_HELMET);
                case "iron" -> new ItemStack(Items.IRON_HELMET);
                case "chainmail" -> new ItemStack(Items.CHAINMAIL_HELMET);
                case "leather" -> new ItemStack(Items.LEATHER_HELMET);
                case "diamond" -> new ItemStack(Items.DIAMOND_HELMET);
                case "netherite" -> new ItemStack(Items.NETHERITE_HELMET);
                default -> ItemStack.EMPTY;
            };
            case "chestplate" -> switch (material) {
                case "gold" -> new ItemStack(Items.GOLDEN_CHESTPLATE);
                case "iron" -> new ItemStack(Items.IRON_CHESTPLATE);
                case "chainmail" -> new ItemStack(Items.CHAINMAIL_CHESTPLATE);
                case "leather" -> new ItemStack(Items.LEATHER_CHESTPLATE);
                case "diamond" -> new ItemStack(Items.DIAMOND_CHESTPLATE);
                case "netherite" -> new ItemStack(Items.NETHERITE_CHESTPLATE);
                default -> ItemStack.EMPTY;
            };
            case "leggings" -> switch (material) {
                case "gold" -> new ItemStack(Items.GOLDEN_LEGGINGS);
                case "iron" -> new ItemStack(Items.IRON_LEGGINGS);
                case "chainmail" -> new ItemStack(Items.CHAINMAIL_LEGGINGS);
                case "leather" -> new ItemStack(Items.LEATHER_LEGGINGS);
                case "diamond" -> new ItemStack(Items.DIAMOND_LEGGINGS);
                case "netherite" -> new ItemStack(Items.NETHERITE_LEGGINGS);
                default -> ItemStack.EMPTY;
            };
            case "boots" -> switch (material) {
                case "gold" -> new ItemStack(Items.GOLDEN_BOOTS);
                case "iron" -> new ItemStack(Items.IRON_BOOTS);
                case "chainmail" -> new ItemStack(Items.CHAINMAIL_BOOTS);
                case "leather" -> new ItemStack(Items.LEATHER_BOOTS);
                case "diamond" -> new ItemStack(Items.DIAMOND_BOOTS);
                case "netherite" -> new ItemStack(Items.NETHERITE_BOOTS);
                default -> ItemStack.EMPTY;
            };
            default -> ItemStack.EMPTY;
        };
    }

    // Hilfsfunktion: Armor-Verzauberungen anwenden
    private void applyArmorEnchantments(ServerLevel serverLevel,
                                        java.util.UUID uuid,
                                        ItemStack stack,
                                        List<String> enchants,
                                        List<String> enchantLevels) {
        if (stack == null || enchants == null || enchantLevels == null) {
            return;
        }
        Random random = new Random(uuid.getLeastSignificantBits());
        enchants.forEach((enchant) -> {
            int level = Integer.parseInt(enchantLevels.get(random.nextInt(enchantLevels.size())));
            ResourceKey<Enchantment> enchantmentKey = switch (enchant) {
                case "protection" -> Enchantments.PROTECTION;
                case "fire_protection" -> Enchantments.FIRE_PROTECTION;
                case "blast_protection" -> Enchantments.BLAST_PROTECTION;
                case "projectile_protection" -> Enchantments.PROJECTILE_PROTECTION;
                case "respiration" -> Enchantments.RESPIRATION;
                case "aqua_affinity" -> Enchantments.AQUA_AFFINITY;
                case "thorns" -> Enchantments.THORNS;
                case "feather_falling" -> Enchantments.FEATHER_FALLING;
                case "depth_strider" -> Enchantments.DEPTH_STRIDER;
                case "frost_walker" -> Enchantments.FROST_WALKER;
                case "binding_curse" -> Enchantments.BINDING_CURSE;
                default -> null;
            };
            if (enchantmentKey != null) {
                var registry = serverLevel.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                var optHolder = registry.getHolder(enchantmentKey);
                var allEnchantments = stack.getAllEnchantments(registry.asLookup());
                if (optHolder.isPresent()) {
                    Enchantment newEnchant = optHolder.get().value();
                    if (newEnchant.canEnchant(stack)
                            && EnchantmentHelper.isEnchantmentCompatible(allEnchantments.keySet(), optHolder.get())) {
                        stack.enchant(optHolder.get(), level);
                    }
                }
            }
        });
    }
}
