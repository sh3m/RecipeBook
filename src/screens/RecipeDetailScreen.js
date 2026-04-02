import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  ScrollView,
  Image,
  TouchableOpacity,
  StyleSheet,
  Alert,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { useTheme } from '../theme/ThemeContext';
import SectionWrapper from '../components/SectionWrapper';
import { loadRecipes, deleteRecipe } from '../storage/RecipeStorage';

export default function RecipeDetailScreen({ navigation, route }) {
  const { colors } = useTheme();
  const initialRecipe = route.params?.recipe;
  const [recipe, setRecipe] = useState(initialRecipe);

  // Reload recipe when returning from edit
  useFocusEffect(
    useCallback(() => {
      loadRecipes().then(all => {
        const found = all.find(r => r.id === initialRecipe.id);
        if (found) setRecipe(found);
      });
    }, [initialRecipe.id])
  );

  const handleDelete = () => {
    Alert.alert(
      'Delete Recipe',
      `Delete "${recipe.name}"? This cannot be undone.`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            await deleteRecipe(recipe.id);
            navigation.goBack();
          },
        },
      ]
    );
  };

  if (!recipe) return null;

  return (
    <View style={[styles.container, { backgroundColor: colors.bg }]}>
      <ScrollView contentContainerStyle={styles.scroll}>
        {/* Recipe wrapper: image + name */}
        <SectionWrapper title={recipe.name}>
          {recipe.image ? (
            <Image source={{ uri: recipe.image }} style={[styles.heroImage, { borderColor: colors.border }]} />
          ) : (
            <View style={[styles.heroPlaceholder, { backgroundColor: colors.surface2 }]}>
              <Text style={styles.heroPlaceholderIcon}>🍽</Text>
            </View>
          )}

          {!!recipe.description && (
            <Text style={[styles.description, { color: colors.text }]}>{recipe.description}</Text>
          )}
        </SectionWrapper>

        {/* Ingredients */}
        {recipe.ingredients?.length > 0 && (
          <SectionWrapper title="Ingredients">
            {recipe.ingredients.map((item, index) => (
              <View
                key={item.id || index}
                style={[styles.ingredientRow, { borderBottomColor: colors.border },
                  index === recipe.ingredients.length - 1 && styles.lastRow]}
              >
                <Text style={[styles.ingredientName, { color: colors.text }]}>
                  {item.ingredient}
                </Text>
                <Text style={[styles.ingredientAmount, { color: colors.accent }]}>
                  {item.amount}
                </Text>
              </View>
            ))}
          </SectionWrapper>
        )}

        {/* Steps */}
        {recipe.steps?.length > 0 && (
          <SectionWrapper title="Instructions">
            {recipe.steps.map((item, index) => (
              <View key={item.id || index} style={styles.stepRow}>
                <View style={[styles.stepBadge, { backgroundColor: colors.accent }]}>
                  <Text style={styles.stepBadgeText}>{index + 1}</Text>
                </View>
                <Text style={[styles.stepText, { color: colors.text }]}>{item.text}</Text>
              </View>
            ))}
          </SectionWrapper>
        )}

        {/* Action buttons */}
        <View style={styles.actions}>
          <TouchableOpacity
            style={[styles.editBtn, { borderColor: colors.accent }]}
            onPress={() => navigation.navigate('AddRecipe', { recipe })}
            activeOpacity={0.8}
          >
            <Text style={[styles.editBtnText, { color: colors.accent }]}>Edit Recipe</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.deleteBtn, { borderColor: colors.danger }]}
            onPress={handleDelete}
            activeOpacity={0.8}
          >
            <Text style={[styles.deleteBtnText, { color: colors.danger }]}>Delete</Text>
          </TouchableOpacity>
        </View>

        <View style={{ height: 32 }} />
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scroll: {
    paddingTop: 16,
  },
  heroImage: {
    width: '100%',
    height: 220,
    borderRadius: 10,
    borderWidth: 1,
    marginBottom: 12,
  },
  heroPlaceholder: {
    width: '100%',
    height: 160,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
  },
  heroPlaceholderIcon: {
    fontSize: 48,
  },
  description: {
    fontSize: 15,
    lineHeight: 22,
  },
  ingredientRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
  },
  lastRow: {
    borderBottomWidth: 0,
  },
  ingredientName: {
    fontSize: 15,
    flex: 1,
  },
  ingredientAmount: {
    fontSize: 14,
    fontWeight: '600',
    marginLeft: 12,
  },
  stepRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 14,
  },
  stepBadge: {
    width: 26,
    height: 26,
    borderRadius: 13,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    marginTop: 1,
    flexShrink: 0,
  },
  stepBadgeText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '700',
  },
  stepText: {
    flex: 1,
    fontSize: 15,
    lineHeight: 22,
  },
  actions: {
    flexDirection: 'row',
    marginHorizontal: 16,
    gap: 12,
    marginBottom: 8,
  },
  editBtn: {
    flex: 1,
    height: 50,
    borderRadius: 10,
    borderWidth: 1.5,
    alignItems: 'center',
    justifyContent: 'center',
  },
  editBtnText: {
    fontSize: 15,
    fontWeight: '700',
  },
  deleteBtn: {
    height: 50,
    paddingHorizontal: 24,
    borderRadius: 10,
    borderWidth: 1.5,
    alignItems: 'center',
    justifyContent: 'center',
  },
  deleteBtnText: {
    fontSize: 15,
    fontWeight: '700',
  },
});
