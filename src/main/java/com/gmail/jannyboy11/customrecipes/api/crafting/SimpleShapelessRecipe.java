package com.gmail.jannyboy11.customrecipes.api.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.jannyboy11.customrecipes.api.InventoryUtils;
import com.gmail.jannyboy11.customrecipes.api.crafting.vanilla.ingredient.ChoiceIngredient;
import com.gmail.jannyboy11.customrecipes.api.crafting.vanilla.recipe.ShapelessRecipe;

public final class SimpleShapelessRecipe implements ShapelessRecipe {
	
	private final NamespacedKey key;
	private ItemStack result;
	private List<ChoiceIngredient> ingredients = new ArrayList<>();
	private boolean hidden;
	private String group = "";
	
	public SimpleShapelessRecipe(NamespacedKey key) {
		this.key = key;
	}
	
	public SimpleShapelessRecipe(NamespacedKey key, ItemStack result) {
		this(key);
		this.result = result;
	}
	
	public SimpleShapelessRecipe(NamespacedKey key, ItemStack result, List<? extends ChoiceIngredient> ingredients) {
		this(key, result);
		setIngredients(ingredients);
	}
	
	public void setResult(ItemStack result) {
		this.result = result;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public void setGroup(String group) {
		this.group = group == null ? "" : group;
	}
	
	public void setIngredients(List<? extends ChoiceIngredient> ingredients) {
		Objects.requireNonNull(ingredients);
		if (ingredients.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("ingredients cannot be null");
		
		this.ingredients = new ArrayList<>(ingredients);
	}
	

	@Override
	public boolean matches(CraftingInventory craftingInventory, World world) {
		final List<ChoiceIngredient> ingredients = new ArrayList<>(this.ingredients);
		final List<ItemStack> contents = Arrays.asList(craftingInventory.getMatrix())
				.stream().filter(i -> InventoryUtils.isEmptyStack(i))
				.collect(Collectors.toList());
		
		for (ItemStack stack : contents) {
			boolean match = false;
			for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
				ChoiceIngredient ingredient = ingredients.get(ingredientIndex);
				if (ingredient.isIngredient(stack)) {
					ingredients.remove(ingredientIndex);
					match = true;
					break;
				}
			}
			
			//there was no matching ingredient for the current itemstack
			if (!match) return false;
		}
		
		//return true if there are no unused ingredients leftover
		return ingredients.isEmpty();
	}

	@Override
	public ItemStack craftItem(CraftingInventory craftingInventory) {
		return result == null ? null : result.clone();
	}

	@Override
	public ItemStack getResult() {
		return result;
	}

	@Override
	public List<? extends ItemStack> getLeftOverItems(CraftingInventory craftingInventory) {
		return Arrays.stream(craftingInventory.getMatrix())
				.map(itemStack -> {
					if (itemStack == null || itemStack.getAmount() <= 1) return null;
					ItemStack clone = itemStack.clone();
					clone.setAmount(itemStack.getAmount() - 1);
					return clone;
				}).collect(Collectors.toList());
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public NamespacedKey getKey() {
		return key;
	}

	@Override
	public List<? extends ChoiceIngredient> getIngredients() {
		return Collections.unmodifiableList(ingredients);
	}

	@Override
	public String getGroup() {
		return group;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof ShapelessRecipe)) return false;
		ShapelessRecipe that = (ShapelessRecipe) o;
		
		return Objects.equals(this.key, that.getKey()) && Objects.equals(this.result, that.getResult()) &&
				Objects.equals(this.ingredients, that.getIngredients()) && Objects.equals(this.hidden, that.isHidden()) &&
				Objects.equals(this.group, that.getGroup());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(key, result, ingredients, hidden, group);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "{" + 
			"key=" + key +
			",result=" + result +
			",ingredients=" + ingredients +
			",hidden=" + hidden +
			",group=" + group +				
			"}";
	}

}
