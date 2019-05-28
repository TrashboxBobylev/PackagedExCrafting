package thelm.packagedexcrafting.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.blakebr0.extendedcrafting.crafting.table.TableRecipeManager;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.container.ContainerEmpty;
import thelm.packagedauto.util.PatternHelper;

public class RecipeInfoAdvanced implements IRecipeInfoTiered {

	IRecipe recipe;
	List<ItemStack> input = new ArrayList<>();
	InventoryCrafting matrix = new InventoryCrafting(new ContainerEmpty(), 5, 5);
	ItemStack output;
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		input.clear();
		output = ItemStack.EMPTY;
		patterns.clear();
		List<ItemStack> matrixList = new ArrayList<>();
		MiscUtil.loadAllItems(nbt.getTagList("Matrix", 10), matrixList);
		for(int i = 0; i < 25 && i < matrixList.size(); ++i) {
			matrix.setInventorySlotContents(i, matrixList.get(i));
		}
		for(Object obj : TableRecipeManager.getInstance().getRecipes(5)) {
			if(obj instanceof IRecipe) {
				IRecipe recipe = (IRecipe)obj;
				if(recipe.matches(matrix, null)) {
					this.recipe = recipe;
					break;
				}
			}
		}
		if(recipe != null) {
			MiscUtil.loadAllItems(nbt.getTagList("Input", 10), input);
			output = recipe.getCraftingResult(matrix).copy();
			for(int i = 0; i*9 < input.size(); ++i) {
				patterns.add(new PatternHelper(this, i));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList inputTag = MiscUtil.saveAllItems(new NBTTagList(), input);
		nbt.setTag("Input", inputTag);
		List<ItemStack> matrixList = new ArrayList<>();
		for(int i = 0; i < 25; ++i) {
			matrixList.add(matrix.getStackInSlot(i));
		}
		NBTTagList matrixTag = MiscUtil.saveAllItems(new NBTTagList(), matrixList);
		nbt.setTag("Matrix", matrixTag);
		return nbt;
	}

	@Override
	public IRecipeType getRecipeType() {
		return RecipeTypeAdvanced.INSTANCE;
	}

	@Override
	public int getTier() {
		return 2;
	}

	@Override
	public boolean isValid() {
		return recipe != null;
	}

	@Override
	public List<IPackagePattern> getPatterns() {
		return Collections.unmodifiableList(patterns);
	}

	@Override
	public List<ItemStack> getInputs() {
		return Collections.unmodifiableList(input);
	}

	@Override
	public ItemStack getOutput() {
		return output.copy();
	}

	@Override
	public IRecipe getRecipe() {
		return recipe;
	}

	@Override
	public InventoryCrafting getMatrix() {
		return matrix;
	}

	@Override
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
		recipe = null;
		this.input.clear();
		patterns.clear();
		int[] slotArray = RecipeTypeAdvanced.SLOTS.toIntArray();
		for(int i = 0; i < 25; ++i) {
			ItemStack toSet = input.get(slotArray[i]);
			toSet.setCount(1);
			matrix.setInventorySlotContents(i, toSet.copy());
		}
		for(Object obj : TableRecipeManager.getInstance().getRecipes(5)) {
			if(obj instanceof IRecipe) {
				IRecipe recipe = (IRecipe)obj;
				if(recipe.matches(matrix, world)) {
					this.recipe = recipe;
					this.input.addAll(MiscUtil.condenseStacks(input));
					this.output = recipe.getCraftingResult(matrix).copy();
					for(int i = 0; i*9 < this.input.size(); ++i) {
						patterns.add(new PatternHelper(this, i));
					}
					return;
				}
			}
		}
		matrix.clear();
	}

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		int[] slotArray = RecipeTypeBasic.SLOTS.toIntArray();
		for(int i = 0; i < 25; ++i) {
			map.put(slotArray[i], matrix.getStackInSlot(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RecipeInfoAdvanced) {
			RecipeInfoAdvanced other = (RecipeInfoAdvanced)obj;
			for(int i = 0; i < input.size(); ++i) {
				if(!ItemStack.areItemStacksEqualUsingNBTShareTag(input.get(i), other.input.get(i))) {
					return false;
				}
			}
			return recipe.equals(other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		Object[] toHash = new Object[2];
		Object[] inputArray = new Object[input.size()];
		for(int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.get(i);
			inputArray[i] = new Object[] {stack.getItem(), stack.getItemDamage(), stack.getCount(), stack.getTagCompound()};
		}
		toHash[0] = recipe;
		toHash[1] = inputArray;
		return Arrays.deepHashCode(toHash);
	}
}