/*
 * JEI Integration Category.
 */
package com.infinityraider.agricraft.compatibility.jei.mutation;

import com.infinityraider.agricraft.init.AgriCraftBlocks;
import java.awt.Point;
import java.util.Collection;
import javax.annotation.Nonnull;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 *
 * @author RlonRyan
 */
public class MutationRecipeCategory implements IRecipeCategory {

	private final IDrawableStatic background;
	private final String localizedName;
	private final IDrawableStatic overlay;

	public MutationRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		// TODO: Localize!
		localizedName = "Crop Mutation";
		overlay = guiHelper.createDrawable(
				new ResourceLocation("agricraft", "textures/gui/jei/cropMutation.png"),
				0, 0, 150, 110
		);
	}

	@Nonnull
	@Override
	public String getUid() {
		return "agricraft.mutation";
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		overlay.draw(minecraft);
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		if (!(recipeWrapper instanceof MutationRecipeWrapper)) {
			return;
		}
		MutationRecipeWrapper wrapper = ((MutationRecipeWrapper) recipeWrapper);

		recipeLayout.getItemStacks().init(0, true, 64, 52);
		recipeLayout.getItemStacks().set(0, new ItemStack(AgriCraftBlocks.blockCrop));

		int index = 1;
		double angleBetweenEach = 360.0 / wrapper.getInputs().size();
		Point point = new Point(64, 20), center = new Point(64, 52);

		for (Object o : wrapper.getInputs()) {
			recipeLayout.getItemStacks().init(index, true, point.x, point.y);
			if (o instanceof Collection) {
				recipeLayout.getItemStacks().set(index, ((Collection<ItemStack>) o));
			}
			if (o instanceof ItemStack) {
				recipeLayout.getItemStacks().set(index, ((ItemStack) o));
			}
			index += 1;
			point = rotatePointAbout(point, center, angleBetweenEach);
		}

		recipeLayout.getItemStacks().init(index, false, 103, 17);
		recipeLayout.getItemStacks().set(index, wrapper.getOutputs().get(0));
	}

	private Point rotatePointAbout(Point in, Point about, double degrees) {
		double rad = degrees * Math.PI / 180.0;
		double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
		double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
		return new Point(((int) newX), ((int) newY));
	}
}
