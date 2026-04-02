import AsyncStorage from '@react-native-async-storage/async-storage';

const RECIPES_KEY = 'recipebook_recipes';

export async function loadRecipes() {
  try {
    const json = await AsyncStorage.getItem(RECIPES_KEY);
    return json ? JSON.parse(json) : [];
  } catch {
    return [];
  }
}

export async function saveRecipes(recipes) {
  try {
    await AsyncStorage.setItem(RECIPES_KEY, JSON.stringify(recipes));
  } catch {
    // storage write failed silently
  }
}

export async function addRecipe(recipe) {
  const recipes = await loadRecipes();
  const updated = [recipe, ...recipes];
  await saveRecipes(updated);
  return updated;
}

export async function updateRecipe(recipe) {
  const recipes = await loadRecipes();
  const updated = recipes.map(r => (r.id === recipe.id ? recipe : r));
  await saveRecipes(updated);
  return updated;
}

export async function deleteRecipe(id) {
  const recipes = await loadRecipes();
  const updated = recipes.filter(r => r.id !== id);
  await saveRecipes(updated);
  return updated;
}
