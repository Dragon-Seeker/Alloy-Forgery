package wraith.alloyforgery.forges;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public record ForgeDefinition(int forgeTier,
                              float speedMultiplier,
                              int fuelCapacity,
                              int maxSmeltTime,
                              Block material,
                              ImmutableList<Block> additionalMaterials) {

    private static final int BASE_MAX_SMELT_TIME = 200;

    private ForgeDefinition(int forgeTier, float speedMultiplier, int fuelCapacity, Block material, ImmutableList<Block> additionalMaterials) {
        this(forgeTier, speedMultiplier, fuelCapacity, (int) (BASE_MAX_SMELT_TIME / speedMultiplier), material, additionalMaterials);
    }

    public static ForgeDefinition fromJson(JsonObject json) {

        final int forgeTier = JsonHelper.getInt(json, "tier");
        final float speedMultiplier = JsonHelper.getFloat(json, "speed_multiplier", 1);
        final int fuelCapacity = JsonHelper.getInt(json, "fuel_capacity", 48000);

        final Block material = Registry.BLOCK.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json, "material"))).orElseThrow(() -> new JsonSyntaxException("Invalid block: " + JsonHelper.getString(json, "material")));

        final var materialMapBuilder = ImmutableList.<Block>builder();
        JsonHelper.getArray(json, "additional_materials", new JsonArray()).forEach(jsonElement -> materialMapBuilder.add(Registry.BLOCK.getOrEmpty(Identifier.tryParse(jsonElement.getAsString())).orElseThrow(() -> new JsonSyntaxException("Invalid block: " + jsonElement))));

        return new ForgeDefinition(forgeTier, speedMultiplier, fuelCapacity, material, materialMapBuilder.build());
    }

    public boolean isBlockValid(Block block) {
        return block == material || this.additionalMaterials.contains(block);
    }

    public ShapedRecipe generateRecipe(Identifier id) {

        final var ingredients = DefaultedList.ofSize(9, Ingredient.EMPTY);
        for (int i = 0; i < 9; i++) {
            ingredients.set(i, Ingredient.ofItems(material));
        }

        ingredients.set(4, Ingredient.ofItems(Blocks.BLAST_FURNACE));

        return new ShapedRecipe(id, "", 3, 3, ingredients, new ItemStack(ForgeRegistry.getControllerBlock(id).get()));

    }

    @Override
    public String toString() {
        return "ForgeDefinition{" +
                "forgeTier=" + forgeTier +
                ", speedMultiplier=" + speedMultiplier +
                ", fuelCapacity=" + fuelCapacity +
                ", maxSmeltTime=" + maxSmeltTime +
                ", material=" + material +
                ", additionalMaterials=" + additionalMaterials +
                '}';
    }
}
