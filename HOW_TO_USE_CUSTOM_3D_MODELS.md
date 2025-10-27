# How to Use Custom 3D Models in Your Quiz App

## ✅ Your Project Already Has This Feature!

Your quiz app is already set up to use custom 3D models. Here's exactly how it works:

## 📁 Current Models

You already have these models in `app/src/main/assets/models/`:
- ✅ `character.obj` - Simple character (currently used in questions)
- ✅ `cartoon_character.obj` - More detailed character (currently used in results)
- ✅ `Obj.obj` - Additional model

## 🎯 How to Switch Models

### In PlaceQuizScreen.kt

The quiz screen loads models based on the `modelFilename` parameter. Here's where to change it:

**Location 1: During Quiz (Line ~246)**
```kotlin
WebGLCharacterView(
    animationState = characterAnimationState,
    modifier = Modifier.fillMaxSize(),
    modelFilename = "character.obj" // Change this filename
)
```

**Location 2: Results Screen (Line ~553)**
```kotlin
WebGLCharacterView(
    animationState = characterAnimationState,
    modifier = Modifier.fillMaxSize(),
    modelFilename = "cartoon_character.obj" // Change this filename
)
```

## 📝 Quick Example: Use Different Models

**Example 1: Use cartoon_character.obj during quiz**
```kotlin
modelFilename = "cartoon_character.obj"
```

**Example 2: Use Obj.obj in results**
```kotlin
modelFilename = "Obj.obj"
```

**Example 3: No custom model (uses built-in default)**
```kotlin
modelFilename = null
```

## ➕ Adding Your Own Custom Model

### Step 1: Get a 3D Model

Download an OBJ file from:
- **Sketchfab**: https://sketchfab.com (filter by "Download" and "OBJ format")
- **Free3D**: https://free3d.com
- **CGTrader**: https://www.cgtrader.com/free-3d-models

Recommended search terms:
- "low poly character"
- "cartoon character"
- "simple character"
- "mascot"

### Step 2: Place Your Model

Put your `.obj` file in:
```
app/src/main/assets/models/your_model.obj
```

**Example:**
```
app/src/main/assets/models/
  ├── character.obj
  ├── cartoon_character.obj
  ├── my_robot.obj  ← Your new model
  └── student.obj   ← Another model
```

### Step 3: Use Your Model in Code

```kotlin
WebGLCharacterView(
    animationState = characterAnimationState,
    modifier = Modifier.fillMaxSize(),
    modelFilename = "my_robot.obj" // Your model!
)
```

## 🎨 Quick Test

Try this right now:

1. **Open** `PlaceQuizScreen.kt`
2. **Find line 246** (in QuizContent)
3. **Change** `"character.obj"` to `"cartoon_character.obj"`
4. **Run** the app
5. **Start a quiz** and see the different character!

## 🎯 Model Requirements

For best results, your OBJ model should:
- ✅ Be in OBJ format (`.obj` file extension)
- ✅ Have triangulated faces (not quads)
- ✅ Include vertices, normals, and texture coordinates
- ✅ Be reasonably scaled (not too large or small)
- ✅ Have low polygon count (500-2000 triangles for smooth performance)

### Sample OBJ File Structure

Your OBJ file should look like:
```
v -0.5 0.5 0.5        # Vertex positions
v 0.5 0.5 0.5
v 0.5 -0.5 0.5

vt 0.0 0.0            # Texture coordinates (optional)
vt 1.0 0.0
vt 1.0 1.0

vn 0.0 1.0 0.0        # Normals (optional)
vn 0.0 1.0 0.0
vn 0.0 1.0 0.0

f 1/1/1 2/2/1 3/3/1  # Faces (vertices/texture/normals)
```

## 🔧 Using Models with Animations

Your models automatically work with all animation states:

```kotlin
enum class CharacterAnimationState {
    IDLE,              // Floating gently
    THINKING,          // Pondering/thinking
    HAPPY,             // Happy expression
    SAD,               // Sad expression
    CELEBRATING,       // Victory celebration!
    ENCOURAGING,       // Motivating the user
    CORRECT_ANSWER,    // Quick celebration for correct
    WRONG_ANSWER       // Shake animation for wrong
}
```

The animations are applied to your model automatically!

## 💡 Pro Tips

### 1. Use Different Models for Different Quiz Types

```kotlin
// In your quiz logic
val modelToUse = when (quizType) {
    QUIZ_TYPE_HISTORY -> "professor.obj"
    QUIZ_TYPE_SCIENCE -> "scientist.obj"
    QUIZ_TYPE_SPORTS -> "athlete.obj"
    else -> "character.obj"
}

WebGLCharacterView(
    animationState = characterAnimationState,
    modelFilename = modelToUse
)
```

### 2. Load Models Based on Performance

```kotlin
// Use simpler models on low-end devices
val modelFilename = if (isLowEndDevice) {
    "simple_character.obj"
} else {
    "detailed_character.obj"
}
```

### 3. Try Your Models

You can quickly test all your models:
1. Change `modelFilename` in the code
2. Run the app
3. Start a quiz
4. See how your model looks!

## 🎯 Complete Example

Here's a complete example showing how to use a custom model:

```kotlin
@Composable
fun MyQuizWithCustomModel() {
    // The animation state
    val animationState = remember { mutableStateOf(CharacterAnimationState.IDLE) }
    
    // Your custom model
    WebGLCharacterView(
        animationState = animationState.value,
        modelFilename = "my_awesome_character.obj", // Your model!
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
    
    // Change animation based on user interaction
    Button(onClick = { 
        animationState.value = CharacterAnimationState.CELEBRATING 
    }) {
        Text("Celebrate!")
    }
}
```

## 📍 File Locations Summary

```
app/
├── src/main/
│   ├── assets/models/          ← Put your .obj files here
│   │   ├── character.obj       ← Current default
│   │   ├── cartoon_character.obj ← Current results
│   │   └── your_model.obj      ← Add your own!
│   └── java/.../ui/
│       ├── components/
│       │   ├── WebGLCharacterView.kt  ← Character display
│       │   ├── CharacterModel.kt      ← Loads models
│       │   └── ModelLoader.kt         ← OBJ file parser
│       └── screens/
│           └── PlaceQuizScreen.kt     ← Quiz screen
```

## ✅ Quick Checklist

- ✅ Models go in `app/src/main/assets/models/`
- ✅ Use `.obj` file format
- ✅ Reference by filename in code
- ✅ Automatically works with all animations
- ✅ Test by running the app

## 🚀 Next Steps

1. **Try existing models** - Switch between `character.obj` and `cartoon_character.obj`
2. **Download your own** - Get a model from Sketchfab
3. **Place in models folder** - Put it in `app/src/main/assets/models/`
4. **Update filename** - Change `modelFilename` in the code
5. **Run and see!** - Test your custom model

---

**That's it!** Your project is already set up for custom 3D models. Just change the filename and add your models to the `models/` folder!

Need more help? Check the files:
- `ModelLoader.kt` - How models are loaded
- `CharacterModel.kt` - Model structure
- `WebGLCharacterView.kt` - How it's displayed

