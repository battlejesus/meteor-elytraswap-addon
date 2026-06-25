package com.elytraswap;

import com.elytraswap.modules.ElytraSwap;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import meteordevelopment.meteorclient.systems.modules.Categories;

public class ElytraSwapAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    /*public static final Category CATEGORY = new Category("Extras");*/

    @Override
    public void onInitialize() {
        LOG.info("Initializing Elytra Swap Addon");
        Modules.get().add(new ElytraSwap());
    }

    /*
    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }
    */

    @Override
    public String getPackage() {
        return "com.elytraswap";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("battlejesus", "meteor-elytraswap-addon"); // Güncelle
    }
}