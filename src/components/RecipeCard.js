import React from 'react';
import { View, Text, Image, TouchableOpacity, StyleSheet } from 'react-native';
import { useTheme } from '../theme/ThemeContext';

export default function RecipeCard({ recipe, onPress }) {
  const { colors } = useTheme();

  return (
    <TouchableOpacity
      style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.border }]}
      onPress={onPress}
      activeOpacity={0.75}
    >
      <View style={styles.thumbnail}>
        {recipe.image ? (
          <Image source={{ uri: recipe.image }} style={styles.image} />
        ) : (
          <View style={[styles.imagePlaceholder, { backgroundColor: colors.surface2 }]}>
            <Text style={[styles.placeholderIcon, { color: colors.hint }]}>🍽</Text>
          </View>
        )}
      </View>
      <View style={styles.info}>
        <Text style={[styles.name, { color: colors.text }]} numberOfLines={1}>
          {recipe.name}
        </Text>
        {!!recipe.description && (
          <Text style={[styles.description, { color: colors.hint }]} numberOfLines={2}>
            {recipe.description}
          </Text>
        )}
        <View style={styles.meta}>
          <Text style={[styles.metaText, { color: colors.accent }]}>
            {recipe.ingredients?.length || 0} ingredients · {recipe.steps?.length || 0} steps
          </Text>
        </View>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    marginHorizontal: 16,
    marginVertical: 6,
    borderRadius: 12,
    borderWidth: 1,
    overflow: 'hidden',
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.18,
    shadowRadius: 4,
  },
  thumbnail: {
    width: 90,
    height: 90,
  },
  image: {
    width: 90,
    height: 90,
  },
  imagePlaceholder: {
    width: 90,
    height: 90,
    alignItems: 'center',
    justifyContent: 'center',
  },
  placeholderIcon: {
    fontSize: 30,
  },
  info: {
    flex: 1,
    padding: 12,
    justifyContent: 'center',
  },
  name: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  description: {
    fontSize: 13,
    lineHeight: 18,
  },
  meta: {
    marginTop: 6,
  },
  metaText: {
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.3,
  },
});
