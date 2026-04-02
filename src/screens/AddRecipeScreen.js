import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Image,
  StyleSheet,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { useTheme } from '../theme/ThemeContext';
import SectionWrapper from '../components/SectionWrapper';
import { addRecipe, updateRecipe } from '../storage/RecipeStorage';

function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2);
}

export default function AddRecipeScreen({ navigation, route }) {
  const { colors } = useTheme();
  const existing = route.params?.recipe;

  const [name, setName] = useState(existing?.name || '');
  const [description, setDescription] = useState(existing?.description || '');
  const [image, setImage] = useState(existing?.image || null);

  // Ingredients: each item has { id, ingredient, amount }
  const [ingredients, setIngredients] = useState(
    existing?.ingredients?.length
      ? existing.ingredients
      : []
  );

  // Steps: each item has { id, text }
  const [steps, setSteps] = useState(
    existing?.steps?.length
      ? existing.steps
      : []
  );

  const pickImage = async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission needed', 'Please allow access to your photo library.');
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.7,
    });
    if (!result.canceled) {
      setImage(result.assets[0].uri);
    }
  };

  const takePhoto = async () => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission needed', 'Please allow camera access.');
      return;
    }
    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.7,
    });
    if (!result.canceled) {
      setImage(result.assets[0].uri);
    }
  };

  const showImageOptions = () => {
    Alert.alert('Recipe Image', 'Choose a photo source', [
      { text: 'Camera', onPress: takePhoto },
      { text: 'Photo Library', onPress: pickImage },
      { text: 'Cancel', style: 'cancel' },
    ]);
  };

  // Ingredient helpers
  const updateIngredient = (id, field, value) => {
    setIngredients(prev => prev.map(i => i.id === id ? { ...i, [field]: value } : i));
  };

  const addIngredientRow = () => {
    setIngredients(prev => [...prev, { id: generateId(), ingredient: '', amount: '' }]);
  };

  const removeIngredient = (id) => {
    setIngredients(prev => prev.filter(i => i.id !== id));
  };

  // Step helpers
  const updateStep = (id, value) => {
    setSteps(prev => prev.map(s => s.id === id ? { ...s, text: value } : s));
  };

  const addStepRow = () => {
    setSteps(prev => [...prev, { id: generateId(), text: '' }]);
  };

  const removeStep = (id) => {
    setSteps(prev => prev.filter(s => s.id !== id));
  };

  const handleSave = async () => {
    if (!name.trim()) {
      Alert.alert('Missing name', 'Please enter a recipe name.');
      return;
    }

    const filledIngredients = ingredients.filter(
      i => i.ingredient.trim() || i.amount.trim()
    );
    const filledSteps = steps.filter(s => s.text.trim());

    const recipe = {
      id: existing?.id || generateId(),
      name: name.trim(),
      description: description.trim(),
      image,
      ingredients: filledIngredients,
      steps: filledSteps,
      createdAt: existing?.createdAt || Date.now(),
      updatedAt: Date.now(),
    };

    if (existing) {
      await updateRecipe(recipe);
    } else {
      await addRecipe(recipe);
    }

    navigation.goBack();
  };

  const inputStyle = [styles.input, { backgroundColor: colors.surface2, color: colors.text, borderColor: colors.border }];
  const labelStyle = [styles.label, { color: colors.hint }];

  return (
    <KeyboardAvoidingView
      style={{ flex: 1, backgroundColor: colors.bg }}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={styles.scroll}
        keyboardShouldPersistTaps="handled"
      >
        {/* Recipe header section: name + image */}
        <SectionWrapper title="Recipe">
          {/* Image upload */}
          <TouchableOpacity onPress={showImageOptions} activeOpacity={0.8}>
            {image ? (
              <Image source={{ uri: image }} style={[styles.imagePreview, { borderColor: colors.border }]} />
            ) : (
              <View style={[styles.imagePlaceholder, { backgroundColor: colors.surface2, borderColor: colors.border }]}>
                <Text style={[styles.imagePlaceholderIcon, { color: colors.hint }]}>📷</Text>
                <Text style={[styles.imagePlaceholderText, { color: colors.hint }]}>Tap to add photo</Text>
              </View>
            )}
          </TouchableOpacity>

          {image && (
            <TouchableOpacity
              style={[styles.changePhotoBtn, { borderColor: colors.accent }]}
              onPress={showImageOptions}
            >
              <Text style={[styles.changePhotoText, { color: colors.accent }]}>Change photo</Text>
            </TouchableOpacity>
          )}

          {/* Name */}
          <Text style={[labelStyle, { marginTop: 16 }]}>NAME</Text>
          <TextInput
            style={inputStyle}
            placeholder="Recipe name"
            placeholderTextColor={colors.hint}
            value={name}
            onChangeText={setName}
            autoCapitalize="words"
          />

          {/* Description */}
          <Text style={[labelStyle, { marginTop: 12 }]}>DESCRIPTION</Text>
          <TextInput
            style={[inputStyle, styles.multiline]}
            placeholder="Add a description (optional)"
            placeholderTextColor={colors.hint}
            value={description}
            onChangeText={setDescription}
            multiline
            numberOfLines={3}
            textAlignVertical="top"
          />
        </SectionWrapper>

        {/* Ingredients section */}
        <SectionWrapper title="Ingredients">
          {ingredients.map((item, index) => (
            <View key={item.id} style={styles.ingredientRow}>
              <View style={[styles.ingredientCard, { backgroundColor: colors.surface2, borderColor: colors.border }]}>
                <TextInput
                  style={[styles.ingredientInput, { color: colors.text }]}
                  placeholder={`Ingredient ${index + 1}`}
                  placeholderTextColor={colors.hint}
                  value={item.ingredient}
                  onChangeText={v => updateIngredient(item.id, 'ingredient', v)}
                  autoCapitalize="sentences"
                />
                <View style={[styles.dividerV, { backgroundColor: colors.border }]} />
                <TextInput
                  style={[styles.amountInput, { color: colors.text }]}
                  placeholder="Amount"
                  placeholderTextColor={colors.hint}
                  value={item.amount}
                  onChangeText={v => updateIngredient(item.id, 'amount', v)}
                />
                <TouchableOpacity
                  onPress={() => removeIngredient(item.id)}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                  style={styles.removeBtn}
                >
                  <Text style={[styles.removeBtnText, { color: colors.hint }]}>✕</Text>
                </TouchableOpacity>
              </View>
            </View>
          ))}

          {/* Blank add row */}
          <TouchableOpacity
            style={[styles.addRow, { borderColor: colors.border, backgroundColor: colors.surface2 }]}
            onPress={addIngredientRow}
            activeOpacity={0.7}
          >
            <Text style={[styles.addRowText, { color: colors.hint }]}>+ Add ingredient</Text>
          </TouchableOpacity>
        </SectionWrapper>

        {/* Steps section */}
        <SectionWrapper title="Instructions">
          {steps.map((item, index) => (
            <View key={item.id} style={styles.stepRow}>
              <View style={[styles.stepCard, { backgroundColor: colors.surface2, borderColor: colors.border }]}>
                <View style={[styles.stepNumber, { backgroundColor: colors.accent }]}>
                  <Text style={styles.stepNumberText}>{index + 1}</Text>
                </View>
                <TextInput
                  style={[styles.stepInput, { color: colors.text }]}
                  placeholder={`Step ${index + 1}`}
                  placeholderTextColor={colors.hint}
                  value={item.text}
                  onChangeText={v => updateStep(item.id, v)}
                  multiline
                  textAlignVertical="top"
                  autoCapitalize="sentences"
                />
                <TouchableOpacity
                  onPress={() => removeStep(item.id)}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                  style={styles.removeBtn}
                >
                  <Text style={[styles.removeBtnText, { color: colors.hint }]}>✕</Text>
                </TouchableOpacity>
              </View>
            </View>
          ))}

          {/* Blank add row */}
          <TouchableOpacity
            style={[styles.addRow, { borderColor: colors.border, backgroundColor: colors.surface2 }]}
            onPress={addStepRow}
            activeOpacity={0.7}
          >
            <Text style={[styles.addRowText, { color: colors.hint }]}>+ Add step</Text>
          </TouchableOpacity>
        </SectionWrapper>

        {/* Save button */}
        <TouchableOpacity
          style={[styles.saveBtn, { backgroundColor: colors.accent }]}
          onPress={handleSave}
          activeOpacity={0.8}
        >
          <Text style={styles.saveBtnText}>{existing ? 'Save changes' : 'Save recipe'}</Text>
        </TouchableOpacity>

        <View style={{ height: 32 }} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  scroll: {
    paddingTop: 16,
  },
  label: {
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 1,
    marginBottom: 6,
  },
  input: {
    borderRadius: 8,
    borderWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 15,
  },
  multiline: {
    minHeight: 72,
    paddingTop: 10,
  },
  imagePreview: {
    width: '100%',
    height: 200,
    borderRadius: 10,
    borderWidth: 1,
  },
  imagePlaceholder: {
    width: '100%',
    height: 160,
    borderRadius: 10,
    borderWidth: 1,
    borderStyle: 'dashed',
    alignItems: 'center',
    justifyContent: 'center',
  },
  imagePlaceholderIcon: {
    fontSize: 36,
    marginBottom: 8,
  },
  imagePlaceholderText: {
    fontSize: 14,
  },
  changePhotoBtn: {
    alignSelf: 'center',
    marginTop: 8,
    paddingVertical: 6,
    paddingHorizontal: 16,
    borderRadius: 20,
    borderWidth: 1,
  },
  changePhotoText: {
    fontSize: 13,
    fontWeight: '600',
  },
  ingredientRow: {
    marginBottom: 8,
  },
  ingredientCard: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 8,
    borderWidth: 1,
    overflow: 'hidden',
    minHeight: 44,
  },
  ingredientInput: {
    flex: 1,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 14,
  },
  dividerV: {
    width: 1,
    height: '70%',
  },
  amountInput: {
    width: 90,
    paddingHorizontal: 10,
    paddingVertical: 10,
    fontSize: 14,
    textAlign: 'right',
  },
  removeBtn: {
    paddingHorizontal: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  removeBtnText: {
    fontSize: 13,
  },
  addRow: {
    borderRadius: 8,
    borderWidth: 1,
    borderStyle: 'dashed',
    paddingVertical: 12,
    alignItems: 'center',
  },
  addRowText: {
    fontSize: 14,
    fontWeight: '500',
  },
  stepRow: {
    marginBottom: 8,
  },
  stepCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    borderRadius: 8,
    borderWidth: 1,
    overflow: 'hidden',
    minHeight: 44,
  },
  stepNumber: {
    width: 32,
    minHeight: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  stepNumberText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: '700',
  },
  stepInput: {
    flex: 1,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 14,
    minHeight: 44,
  },
  saveBtn: {
    marginHorizontal: 16,
    height: 56,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  saveBtnText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
});
