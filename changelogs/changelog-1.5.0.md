# 1.5.0

- Added Enchantment Numerals: enchantment levels above X no longer show raw text. A Looting 11 book read "Looting enchantment.level.11", because Minecraft only ships names for the first ten levels while levels are legal all the way to 255
- Three styles: Mixed (default) keeps Roman numerals up to X and switches to plain numbers past it; Always Roman generates numerals at every level, XI through CCLV; Always Numbers uses plain numbers everywhere, including 1-10
- Mixed defers to the language file whenever a translation exists, so resource packs that do name high levels keep working. The other two are explicit choices and always generate
- Single-level enchantments like Mending still show no number at all, exactly as in vanilla
- New Enchantment Style section in the Mods menu Config screen and a matching page in the Sodium video settings integration
- Internal: new enchantment/ package plus mixin/EnchantmentMixin. WrapOperation (MixinExtras) wraps the single Component.translatable call inside Enchantment.getFullname and captures the target method's own level parameter rather than parsing it back out of the key, so one injection covers item tooltips, enchanted books and the enchanting table