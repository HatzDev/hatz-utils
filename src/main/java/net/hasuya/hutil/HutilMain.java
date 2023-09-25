package net.hasuya.hutil;

import net.fabricmc.api.ModInitializer;
import net.hasuya.hutil.utils.HutilRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HutilMain implements ModInitializer {
	public static final String MODID = "hutil";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		HutilRegistry.registerRegistries();
	}
}
