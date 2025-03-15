package com.hinaplugin.tradeMobs;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Locale;

public class PlayerInteractListener implements Listener {

    // プレイヤーがエンティティを右クリックしたときのイベント
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event){
        // 右クリックしたプレイヤー
        final Player player = event.getPlayer();

        // 右クリックしたプレイヤーに交易権限がなければ処理終了
        if (!player.hasPermission("trademobs.trade")) {
            return;
        }

        // 右クリックされたエンティティ
        final Entity entity = event.getRightClicked();

        // 取引可能なエンティティ(村人，行商人）は対象外のため処理終了
        if (entity instanceof Merchant){
            return;
        }

        // タイルエンティティ（額縁，防具立て，ボート，トロッコ，絵画）は対象外のため処理終了
        if (!(entity instanceof LivingEntity)){
            return;
        }

        // コンフィグのエンティティリストを取得
        final ConfigurationSection section = TradeMobs.config.getConfigurationSection("mobs");
        if (section == null){
            // コンフィグに何も登録されていない場合は処理終了
            return;
        }

        // 右クリックされたエンティティがコンフィグに登録されているか
        if (!section.contains(entity.getType().toString().toLowerCase(Locale.ROOT))){
            // 右クリックされたエンティティがコンフィグに登録されていない場合は処理終了
            return;
        }

        // コンフィグに登録されている交易情報
        final List<String> tradeSettingList = section.getStringList(entity.getType().toString().toLowerCase(Locale.ROOT));
        if (tradeSettingList.isEmpty()){
            // コンフィグに登録されているエンティティに交易情報が登録されていない場合は処理終了
            return;
        }

        // 交易のメイン情報

        final Merchant merchant = Bukkit.createMerchant(Component.text(entity.getType().toString()));

        // 交易のレシピリスト
        final List<MerchantRecipe> recipeList = Lists.newArrayList();

        // 各交易レシピを作成
        for (final String tradeSetting : tradeSettingList) {
            // 交易で渡すアイテムと受け取るアイテムを分ける
            final String[] trades = tradeSetting.split("-");
            if (trades.length != 2){
                // 渡すアイテムと受け取るアイテムが指定のフォーマットで設定されていない場合は処理終了
                return;
            }

            // 渡すアイテムが複数個ある場合は分ける
            final String[] sellItems = trades[0].split("&");

            // 渡すアイテムが3種類以上の場合は処理終了（渡せるアイテムは最大2種類のため）
            if (sellItems.length > 2){
                return;
            }

            // 受け取るアイテムの情報処理
            final String[] buyItem = trades[1].split("%");

            MerchantRecipe recipe;

            if (buyItem[0].contains("$")){
                // 受け取るアイテム情報を分割
                final String[] potions = buyItem[0].split("\\$");

                // ポーションのフォーマットが不正な値の場合スキップ
                if (potions.length != 3){
                    continue;
                }

                // ポーションの効果
                final PotionEffectType potionEffectType = PotionEffectType.getByName(potions[1].toUpperCase(Locale.ROOT));

                // ポーションのタイプ
                final Material material = potions[2].chars().allMatch(Character::isDigit) ? potions[2].equalsIgnoreCase("1") ? Material.POTION : potions[2].equalsIgnoreCase("2") ? Material.SPLASH_POTION : potions[2].equalsIgnoreCase("3") ? Material.LINGERING_POTION : Material.POTION : Material.POTION;

                // ポーションの効果が存在しなければスキップ
                if (potionEffectType == null){
                    continue;
                }

                // ポーションの効果
                final ItemStack itemStack = this.getItemStack(material, potions[1]);

                // ポーション効果が存在しなければスキップ
                if (itemStack == null){
                    continue;
                }

                // 交易情報に受け取るアイテムを登録
                recipe = new MerchantRecipe(itemStack, Integer.MAX_VALUE);
            }else {
                // 受け取るアイテムの種類
                final Material buyItemMaterial = Material.getMaterial(buyItem[0].toUpperCase(Locale.ROOT));

                // 受け取るアイテムの個数
                final int buyItemAmount = buyItem[1].chars().allMatch(Character::isDigit) ? Integer.parseInt(buyItem[1]) : 1;

                // 受け取るアイテムの種類が存在しなければ処理終了
                if (buyItemMaterial == null){
                    return;
                }

                // 交易情報に受け取るアイテムを登録
                recipe = new MerchantRecipe(new ItemStack(buyItemMaterial, buyItemAmount), Integer.MAX_VALUE);
            }

            if (sellItems.length == 1){
                // 渡すアイテムが1種類の場合
                // 渡すアイテムの情報処理
                final String[] sellItem = sellItems[0].split("%");

                // 渡すアイテムの種類
                final Material sellItemMaterial = Material.getMaterial(sellItem[0].toUpperCase(Locale.ROOT));

                // 渡すアイテムの個数
                final int sellItemAmount = sellItem[1].chars().allMatch(Character::isDigit) ? Integer.parseInt(sellItem[1]) : 1;

                // 渡すアイテムの種類が存在しなければ処理終了
                if (sellItemMaterial == null){
                    return;
                }

                // 交易情報に渡すアイテムを登録
                recipe.setIngredients(Lists.newArrayList(new ItemStack(sellItemMaterial, sellItemAmount)));
            }else {
                // 渡すアイテムが2種類の場合
                // 1種類目の渡すアイテムの情報処理
                final String[] sellItem1 = sellItems[0].split("%");

                // 1種類目の渡すアイテムの種類
                final Material sellItemMaterial1 = Material.getMaterial(sellItem1[0].toUpperCase(Locale.ROOT));

                // 1種類目の渡すアイテムの個数
                final int sellItemAmount1 = sellItem1[1].chars().allMatch(Character::isDigit) ? Integer.parseInt(sellItem1[1]) : 1;

                // 1種類目の渡すアイテムの種類が存在しなければ処理終了
                if (sellItemMaterial1 == null){
                    return;
                }

                // 2種類目の渡すアイテムの情報処理
                final String[] sellItem2 = sellItems[1].split("%");

                // 2種類目の渡すアイテムの種類
                final Material sellItemMaterial2 = Material.getMaterial(sellItem2[0].toUpperCase(Locale.ROOT));

                // 2種類目の渡すアイテムの個数
                final int sellItemAmount2 = sellItem2[1].chars().allMatch(Character::isDigit) ? Integer.parseInt(sellItem2[1]) : 1;

                // 2種類目の渡すアイテムの種類が存在しなければ処理終了
                if (sellItemMaterial2 == null){
                    return;
                }

                // 交易情報にそれぞれの渡すアイテムを登録
                recipe.setIngredients(Lists.newArrayList(new ItemStack(sellItemMaterial1, sellItemAmount1), new ItemStack(sellItemMaterial2, sellItemAmount2)));
            }

            // 交易情報をリストに追加
            recipeList.add(recipe);
        }

        // エンティティに交易情報を登録
        merchant.setRecipes(recipeList);

        // 右クリックしたプレイヤーに交易画面を表示
        player.openMerchant(merchant, true);
    }

    private ItemStack getItemStack(Material material, String potionEffectType) {
        // ポーションのアイテム情報
        final ItemStack itemStack = new ItemStack(material, 1);

        // ポーションの詳細情報
        final PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

        // ベース効果を取得
        if (!EnumUtils.isValidEnum(PotionType.class, potionEffectType.toUpperCase(Locale.ROOT))){
            return null;
        }

        // ポーションのベースを設定
        potionMeta.setBasePotionType(PotionType.valueOf(potionEffectType.toUpperCase(Locale.ROOT)));

        // ポーション効果をポーションアイテムに設定
        itemStack.setItemMeta(potionMeta);
        return itemStack;
    }
}
