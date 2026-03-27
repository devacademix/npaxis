# Design System Specification: The Clinical Atelier

## 1. Overview & Creative North Star: "The Clinical Atelier"
The North Star for this design system is **The Clinical Atelier**. In a field dominated by sterile, "out-of-the-box" Bootstrap clones, we choose a path of high-end editorial precision. We move beyond "SaaS Blue" into a space that feels like a premium medical practice: quiet, authoritative, and meticulously organized.

To break the "template" look, we reject rigid, boxed-in grids. Instead, we utilize **Intentional Asymmetry** and **Tonal Depth**. By layering surfaces of varying luminance and using sophisticated typography scales, we create an environment that feels less like software and more like a high-performance instrument. This is "Modern Medical" through the lens of luxury minimalism.

---

## 2. Colors: Depth over Definition
Our palette is rooted in `primary` (#003d9b), but its power comes from how it interacts with our expansive neutral "Surface" tiers.

### The "No-Line" Rule
**Traditional 1px solid borders are strictly prohibited for sectioning.** To create separation, designers must use background color shifts. 
*   *Example:* A `surface-container-low` (#f2f4f6) sidebar sitting against a `surface` (#f7f9fb) main content area. This creates a sophisticated, "borderless" boundary that feels modern and airy.

### Surface Hierarchy & Nesting
Think of the UI as stacked sheets of frosted glass.
*   **Base:** `surface` (#f7f9fb)
*   **Low Importance/Backgrounds:** `surface-container-low` (#f2f4f6)
*   **Standard Cards/Containers:** `surface-container-lowest` (#ffffff)
*   **High Interaction/Elevated Elements:** `surface-container-high` (#e6e8ea)

### The "Glass & Gradient" Rule
To elevate CTAs and hero moments, use the **Signature Texture**: A subtle linear gradient from `primary` (#003d9b) to `primary_container` (#0052cc) at a 135-degree angle. For floating overlays (modals/popovers), use `surface_container_lowest` at 85% opacity with a `backdrop-filter: blur(12px)`. This "Glassmorphism" prevents the UI from feeling "pasted on" and allows medical data to feel integrated.

---

## 3. Typography: Editorial Authority
We pair **Manrope** (Display/Headlines) with **Inter** (Body/UI) to balance personality with clinical legibility.

*   **Display & Headlines (Manrope):** Large, bold, and airy. These are your "Editorial Anchors." Use `display-lg` (3.5rem) for hero statements to convey confidence. The tight kerning of Manrope adds a "custom-built" feel.
*   **Body & Titles (Inter):** The workhorse. Inter’s high x-height ensures readability of complex medical data. Use `body-md` (0.875rem) as the standard for patient records and lab results.
*   **Labels (Inter):** Small, uppercase with a +5% letter-spacing for `label-sm` (0.6875rem) to ensure technical specifications remain legible even at reduced sizes.

---

## 4. Elevation & Depth: Tonal Layering
We do not use shadows to show "height"; we use tonal shifts to show "importance."

*   **The Layering Principle:** Place a `surface-container-lowest` card on a `surface-container-low` background. The contrast in white-to-off-white creates a "Ghost Lift" that is cleaner than any drop shadow.
*   **Ambient Shadows:** If a floating element (like a dropdown) requires a shadow, use a highly diffused blur: `0px 12px 32px rgba(25, 28, 30, 0.06)`. The shadow must be tinted with the `on_surface` color, never pure black.
*   **The "Ghost Border" Fallback:** If a container *must* have a stroke for accessibility, use `outline_variant` (#c3c6d6) at 20% opacity. It should be felt, not seen.

---

## 5. Components: Precision Primitives

### Cards & Lists
*   **Rule:** Forbid divider lines. 
*   **Execution:** Use `6` (1.5rem) vertical spacing from the scale to separate list items. For cards, use `DEFAULT` (0.5rem) to `lg` (1rem) roundedness. 
*   **Specifics:** A patient profile card should be `surface-container-lowest` with a `lg` corner radius, sitting on a `surface-container-low` dashboard background.

### Buttons
*   **Primary:** Gradient of `primary` to `primary_container`. Corner radius: `full` (pill shape) for high-distinction CTAs.
*   **Secondary:** `surface-container-high` background with `on_secondary_container` text. 
*   **Interaction:** On hover, shift the gradient intensity rather than darkening the color.

### Input Fields
*   **Default State:** Background: `surface_container_low`. No border.
*   **Active State:** Background: `surface_container_lowest`. 2px border of `primary` (#003d9b). 
*   **Logic:** The "sink-to-float" interaction (moving from a greyish background to a pure white background on focus) provides clear visual feedback without clutter.

### Medical-Specific Components
*   **The "Vitals" Chip:** Use `secondary_container` (#b6c8fe) for neutral readings and `tertiary_container` (#a33500) for alerts.
*   **The Data Timeline:** Use a vertical `outline_variant` track at 10% opacity, with `primary` nodes to denote medical events.

---

## 6. Do's and Don'ts

### Do:
*   **Embrace Negative Space:** Use `10` (2.5rem) or `12` (3rem) spacing between major sections to let the "medical mind" breathe.
*   **Use Intentional Asymmetry:** Align primary headers to the left, but offset supporting metadata or secondary actions to create a dynamic, editorial flow.
*   **Color for Intent:** Only use `primary` for actionable elements. If it’s not a link or a button, it shouldn't be blue.

### Don't:
*   **Don't use 100% Black:** Always use `on_surface` (#191c1e) for text. Pure black is too harsh for a professional medical environment.
*   **Don't use "Default" Shadows:** Avoid the Figma default (0, 4, 4, 0). They look cheap and digital.
*   **Don't Box Everything:** If you find yourself putting every piece of information in a bordered box, you have failed the "No-Line" Rule. Use white space and surface shifts instead.