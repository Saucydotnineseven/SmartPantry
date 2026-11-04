from html import escape
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED


OUT = Path("submission/402411462_Thomas.M_MAD700_Assignment.docx")
OUT.parent.mkdir(parents=True, exist_ok=True)

W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
XMLNS = f'xmlns:w="{W}"'


def run(text, *, bold=False, italic=False, size=24, color="18231D", font="Times New Roman"):
    props = f'<w:rPr><w:rFonts w:ascii="{font}" w:hAnsi="{font}" w:eastAsia="{font}"/><w:sz w:val="{size}"/><w:color w:val="{color}"/>'
    if bold:
        props += "<w:b/>"
    if italic:
        props += "<w:i/>"
    props += "</w:rPr>"
    return f"<w:r>{props}<w:t xml:space=\"preserve\">{escape(text)}</w:t></w:r>"


def para(text="", *, bold=False, italic=False, align=None, before=0, after=120,
         color="18231D", size=24, keep=False, line=360):
    ppr = f'<w:pPr><w:spacing w:before="{before}" w:after="{after}" w:line="{line}" w:lineRule="auto"/>'
    if align:
        ppr += f'<w:jc w:val="{align}"/>'
    if keep:
        ppr += "<w:keepNext/>"
    ppr += "</w:pPr>"
    return f"<w:p>{ppr}{run(text, bold=bold, italic=italic, size=size, color=color)}</w:p>"


def rich_para(parts, *, before=0, after=120, align=None, line=360):
    ppr = f'<w:pPr><w:spacing w:before="{before}" w:after="{after}" w:line="{line}" w:lineRule="auto"/>'
    if align:
        ppr += f'<w:jc w:val="{align}"/>'
    ppr += "</w:pPr>"
    runs = "".join(run(text, **opts) for text, opts in parts)
    return f"<w:p>{ppr}{runs}</w:p>"


def page_break():
    return '<w:p><w:r><w:br w:type="page"/></w:r></w:p>'


def hyperlink_para(label, rid):
    return f'<w:p><w:pPr><w:spacing w:after="120" w:line="360" w:lineRule="auto"/></w:pPr><w:hyperlink r:id="{rid}" w:history="1"><w:r><w:rPr><w:rStyle w:val="Hyperlink"/><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/><w:color w:val="1F6B50"/><w:u w:val="single"/></w:rPr><w:t>{escape(label)}</w:t></w:r></w:hyperlink></w:p>'


def cell(text, *, fill=None, bold=False, color="18231D", width=4500, size=24, align=None):
    shade = f'<w:shd w:fill="{fill}"/>' if fill else ""
    tcpr = f'<w:tcPr><w:tcW w:w="{width}" w:type="dxa"/>{shade}<w:tcMar><w:top w:w="100" w:type="dxa"/><w:start w:w="120" w:type="dxa"/><w:bottom w:w="100" w:type="dxa"/><w:end w:w="120" w:type="dxa"/></w:tcMar></w:tcPr>'
    return f"<w:tc>{tcpr}{para(text, bold=bold, color=color, size=size, after=0, align=align)}</w:tc>"


def table(rows, *, widths=None, header=False, fill="E7F0E7"):
    if widths is None:
        widths = [9000 // max(1, len(rows[0]))] * len(rows[0])
    borders = '<w:tblBorders><w:top w:val="single" w:sz="5" w:color="D7E2D8"/><w:left w:val="single" w:sz="5" w:color="D7E2D8"/><w:bottom w:val="single" w:sz="5" w:color="D7E2D8"/><w:right w:val="single" w:sz="5" w:color="D7E2D8"/><w:insideH w:val="single" w:sz="5" w:color="D7E2D8"/><w:insideV w:val="single" w:sz="5" w:color="D7E2D8"/></w:tblBorders>'
    xml = f'<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblLayout w:type="fixed"/>{borders}</w:tblPr>'
    for ri, row in enumerate(rows):
        xml += "<w:tr>"
        for ci, value in enumerate(row):
            is_header = header and ri == 0
            xml += cell(value, fill=fill if is_header else None, bold=is_header, color="164E3B" if is_header else "18231D", width=widths[ci])
        xml += "</w:tr>"
    return xml + "</w:tbl>"


def heading(text):
    return para(text, bold=True, size=26, color="164E3B", before=220, after=140, keep=True)


def body(text):
    return para(text, after=150)


def picture(rid, title):
    width, height = 3246120, 7213600  # 3.55 x 7.89 inches; preserves a 720x1600 screen ratio.
    picture_id = rid.removeprefix("rId")
    return f'''<w:p><w:pPr><w:jc w:val="center"/><w:spacing w:after="80"/></w:pPr><w:r><w:drawing>
    <wp:inline xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" distT="0" distB="0" distL="0" distR="0">
      <wp:extent cx="{width}" cy="{height}"/><wp:docPr id="{picture_id}" name="{escape(title)}"/><wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect="1"/></wp:cNvGraphicFramePr>
      <a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic><pic:nvPicPr><pic:cNvPr id="{picture_id}" name="{escape(title)}"/><pic:cNvPicPr/></pic:nvPicPr>
        <pic:blipFill><a:blip r:embed="{rid}"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>
        <pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="{width}" cy="{height}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr>
      </pic:pic></a:graphicData></a:graphic>
    </wp:inline></w:drawing></w:r></w:p>'''


def placeholder(title, instruction, image_rid=None):
    if image_rid:
        return para(title, bold=True, color="164E3B", before=140, after=60) + body(instruction) + picture(image_rid, title)
    rows = [[title], [instruction], ["PASTE REAL PHONE SCREENSHOT HERE"]]
    # A one-column framed panel makes it easy to replace with an actual image later.
    x = table(rows, widths=[9000])
    return x + para("", after=80)


parts = []

# Recreated cover using the fields and declaration on the assignment PDF cover.
parts += [para("richfield.ac.za", bold=True, color="164E3B", size=26, align="center", before=120, after=80)]
parts += [para("FACULTY OF INFORMATION TECHNOLOGY", bold=True, align="center", after=40)]
parts += [para("MOBILE APP DEVELOPMENT 700", bold=True, align="center", after=40)]
parts += [para("PRACTICAL ASSIGNMENT REPORT", bold=True, color="164E3B", align="center", before=100, after=40)]
parts += [para("SMART PANTRY MANAGER", bold=True, align="center", size=28, after=180)]
parts += [table([
    ["Name and Surname", "Mphoentle Thomas"],
    ["Student ITS No.", "402411462"],
    ["Module", "Mobile App Development 700"],
    ["GitHub repository", "https://github.com/Saucydotnineseven/SmartPantry"],
    ["Year of Study / Qualification / Semester", "[Complete these details]"],
    ["Date submitted", "[Enter submission date]"],
    ["Assignment due date", "[Enter due date]"],
], widths=[4300, 4700])]
parts += [hyperlink_para("Open the Smart Pantry Manager GitHub repository", "rId11")]
parts += [heading("DECLARATION OF ORIGINALITY")]
parts += [body("I hereby declare that this assignment is my own work and has not been copied from any other source except where due acknowledgment is made. I affirm that all sources used have been properly cited and that this submission complies with the institution’s policies on academic integrity and plagiarism.")]
parts += [para("Student Signature: ____________________________________     Date: __________________", after=80)]
parts += [para("Please complete the remaining cover details and sign the declaration before submission.", italic=True, color="68756D", size=22)]
parts += [page_break()]

# Contents page with an updateable Word TOC field.
parts += [heading("TABLE OF CONTENTS")]
parts += ['<w:p><w:fldSimple w:instr="TOC \\o &quot;1-2&quot; \\h \\z \\u"><w:r><w:t>Open in Word and update this field to populate page numbers.</w:t></w:r></w:fldSimple></w:p>']
parts += [body("If the contents list does not populate automatically, click it in Word and choose Update Field → Update entire table.")]
parts += [page_break()]

parts += [heading("1. INTRODUCTION")]
parts += [body("Smart Pantry Manager is a Java Android application designed to help people use ingredients they already have at home. Food can be wasted when people lose track of what is in the pantry or do not know what they can prepare from the available ingredients. The app stores pantry items and compares them with a local collection of recipes.")]
parts += [body("The central requirement is strict recipe matching: a recipe is suggested only when every required ingredient is present in a sufficient quantity. A partial match is not shown as a recipe the user can make. The app works offline and stores pantry data on the device using SQLite.")]
parts += [heading("2. SYSTEM DESIGN")]
parts += [heading("2.1 Screen flow")]
parts += [body("The pantry is the starting screen. The user can add an ingredient, tap an existing row to edit it, or long-press a row to delete it. The Recipes action opens the strict suggestions list; selecting a recipe opens its detail screen. Settings is available from the bottom navigation.")]
parts += [table([
    ["Screen", "User action / next screen"],
    ["Pantry List", "Add → Add/Edit Ingredient; select row → Edit; long-press → Delete confirmation; Recipes → Suggested Recipes; Settings → Settings"],
    ["Add/Edit Ingredient", "Validate fields → save to SQLite → return to Pantry List"],
    ["Suggested Recipes", "Select a fully matched recipe → Recipe Detail; Back → Pantry List"],
    ["Recipe Detail", "Read ingredients and numbered method → Back to Suggested Recipes"],
    ["Settings", "Save expiry alert and unit preferences → Back to Pantry List"],
], widths=[2600, 6400], header=True)]
parts += [heading("2.2 Data model")]
parts += [body("The local SQLite database has three related tables. A pantry row stores a user-owned ingredient. A recipe row stores its name and method. Recipe ingredients connect the catalogue recipes to their required quantities and units.")]
parts += [table([
    ["PANTRY", "RECIPES", "RECIPE_INGREDIENTS"],
    ["id (PK)\nname\nquantity\nunit\nexpiry_date", "id (PK)\nname\nsteps", "id (PK)\nrecipe_id (FK)\nname\nquantity\nunit"],
    ["One row per pantry item", "One row per recipe", "Many ingredient rows belong to one recipe"],
], widths=[3000, 3000, 3000], header=True)]
parts += [body("Relationship: RECIPES.id (one) → RECIPE_INGREDIENTS.recipe_id (many). Pantry items are stored independently because they represent the user's current stock; the matcher compares them with the recipe ingredient rows.")]
parts += [heading("2.3 Main components")]
parts += [table([
    ["Component", "Responsibility"],
    ["DatabaseHelper.java", "Creates/upgrades SQLite tables, seeds the recipe catalogue, and provides pantry CRUD and recipe reads."],
    ["PantryAdapter.java", "Binds pantry records to RecyclerView rows and forwards tap/long-press actions."],
    ["RecipeMatcher.java", "Normalizes basic singular/plural differences, converts supported units, totals pantry stock, and applies the strict all-ingredients check."],
    ["Activities and Intents", "MainActivity, AddEditActivity, SuggestedRecipesActivity, RecipeDetailActivity, and SettingsActivity implement the app screens and pass item/recipe identifiers between screens."],
], widths=[2800, 6200], header=True)]

parts += [heading("3. SCREENSHOTS OF THE RUNNING APP")]
parts += [body("The figures below use screenshots captured from the running app on the phone. Figure 8 remains a placeholder for the recipe detail screen; keep the full screen visible when adding it.")]
screens = [
    ("Figure 1. Pantry list", "Populated pantry and bottom navigation.", "rId3"),
    ("Figure 2. Add ingredient form", "The blank form before saving a new pantry item.", "rId8"),
    ("Figure 3. Input validation", "The invalid expiry date displays the form validation message.", "rId4"),
    ("Figure 4. Edit ingredient", "An existing item is open for editing with its saved name, quantity, and unit.", "rId9"),
    ("Figure 5. Delete confirmation", "Long-pressing a pantry item opens this confirmation before deletion.", "rId5"),
    ("Figure 6. Strict recipe suggestions", "Scrambled Eggs appears when eggs, butter, and salt meet the required quantities.", "rId6"),
    ("Figure 7. Missing ingredient check", "No-match state after removing or reducing a required ingredient.", "rId10"),
    ("Figure 8. Recipe detail", "Capture a selected recipe's complete ingredient list and numbered method.", None),
    ("Figure 9. Settings", "Saved expiry alert and unit preference controls.", "rId7"),
]
for title, instruction, image_rid in screens:
    parts += [page_break(), placeholder(title, instruction, image_rid)]

parts += [heading("4. KEY CODE SNIPPETS")]
parts += [body("The following short examples are taken from the project source and highlight the main Android and business-logic concepts.")]
parts += [para("4.1 Strict matching — RecipeMatcher.java", bold=True, color="164E3B", before=100)]
parts += [table([["for (RecipeIngredient ing : recipe.getIngredients()) {\n    String key = makeKey(ing.getName(), ing.getUnit());\n    double needed = toBaseAmount(ing.getQuantity(), ing.getUnit());\n    Double have = available.get(key);\n    if (have == null || have + 0.0001 < needed) return false;\n}\nreturn true;"]], widths=[9000])]
parts += [body("The matcher checks every required ingredient. A missing item or insufficient quantity immediately rejects the recipe; only after all rows pass does the method return true.")]
parts += [para("4.2 Pantry data persistence — DatabaseHelper.java", bold=True, color="164E3B", before=100)]
parts += [table([["public long addPantryItem(PantryItem item) {\n    SQLiteDatabase db = getWritableDatabase();\n    ContentValues values = new ContentValues();\n    values.put(COL_NAME, item.getName());\n    values.put(COL_QTY, item.getQuantity());\n    values.put(COL_UNIT, item.getUnit());\n    values.put(COL_EXPIRY, item.getExpiryDate());\n    return db.insert(TABLE_PANTRY, null, values);\n}"]], widths=[9000])]
parts += [body("The helper maps a PantryItem to database columns and inserts it into SQLite. Related methods read, update, and delete pantry rows, so user data persists between app sessions.")]
parts += [para("4.3 Activity navigation — MainActivity.java", bold=True, color="164E3B", before=100)]
parts += [table([["Intent intent = new Intent(MainActivity.this, AddEditActivity.class);\nintent.putExtra(AddEditActivity.EXTRA_ITEM_ID, item.getId());\nstartActivity(intent);"]], widths=[9000])]
parts += [body("An explicit Intent opens the add/edit activity. When editing, the pantry item's ID is passed as an extra so the destination can load and update the correct database row.")]
parts += [para("4.4 RecyclerView refresh — MainActivity.java", bold=True, color="164E3B", before=100)]
parts += [table([["protected void onResume() {\n    super.onResume();\n    loadPantry();\n    bottomNav.setSelectedItemId(R.id.nav_pantry);\n    showExpiryAlert();\n}"]], widths=[9000])]
parts += [body("The pantry is reloaded when MainActivity becomes visible again, so an item added or edited on another screen appears in the list when the user returns.")]

parts += [heading("5. CHALLENGES AND SOLUTIONS")]
parts += [body("The examples below are based on the implementation and debugging work completed in this project. Please confirm they match your own experience and edit them in your own words before submission.")]
parts += [para("Challenge 1: A strict matcher must compare quantities even when users enter common unit variants or plural names.\nSolution: RecipeMatcher normalizes case and simple plurals, converts supported weight/volume units to a base unit, and only accepts a recipe after every ingredient passes.", after=160)]
parts += [para("Challenge 2: A recipe catalogue created only during database initialization can be missing on an already-installed app if initialization logic changes.\nSolution: The database version was advanced and the upgrade path now creates missing tables and seeds recipes only when the catalogue is empty, while retaining pantry rows.", after=160)]
parts += [para("Add one more genuine challenge you encountered, what caused it, and how you solved it: [Complete in your own words.]", italic=True, color="68756D")]

parts += [heading("6. CONCLUSION AND REFLECTION")]
parts += [body("Smart Pantry Manager brings together Android activities, explicit intents, RecyclerView adapters, input validation, SharedPreferences, and SQLite persistence to solve a focused food-waste problem. Its key behavior is the strict recipe matcher: suggestions are limited to recipes whose required ingredients and quantities are all available in the pantry.")]
parts += [body("Personal reflection — complete this paragraph with what you learned, which part you found most challenging, and one improvement you would make with more time: [Write your reflection here.] ")]

parts += [heading("7. REFERENCES")]
parts += [para("Richfield Graduate Institute of Technology (2026) Mobile App Development 700: Practical Assignment — Smart Pantry Manager. Assignment brief supplied for the module.", after=130)]
parts += [para("Google (n.d.) SQLiteOpenHelper. Android Developers API reference. Available at: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper (Accessed: 4 November 2026).", after=130)]
parts += [para("Google (n.d.) Create dynamic lists with RecyclerView. Android Developers. Available at: https://developer.android.com/develop/ui/views/layout/recyclerview (Accessed: 4 November 2026).", after=130)]
parts += [para("Google (n.d.) Intents and intent filters. Android Developers. Available at: https://developer.android.com/guide/components/intents-filters (Accessed: 4 November 2026).", after=130)]
parts += [para("Smart Pantry Manager source code (2026) Java Android project. GitHub repository: https://github.com/Saucydotnineseven/SmartPantry (Accessed: 4 November 2026).", after=130)]

document = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document {XMLNS} xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><w:body>{''.join(parts)}<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134" w:header="567" w:footer="567" w:gutter="0"/></w:sectPr></w:body></w:document>'''

styles = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles {XMLNS}><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:line="360" w:lineRule="auto"/></w:pPr></w:pPrDefault></w:docDefaults></w:styles>'''

content_types = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Default Extension="jpg" ContentType="image/jpeg"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/><Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/><Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/><Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/></Types>'''
rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/><Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/></Relationships>'''
document_rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/><Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/pantry_list.jpg"/><Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/edit_validation.jpg"/><Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/delete_confirmation.jpg"/><Relationship Id="rId6" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/suggested_scrambled_eggs.jpg"/><Relationship Id="rId7" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/settings.jpg"/><Relationship Id="rId8" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/add_ingredient.jpg"/><Relationship Id="rId9" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/edit_ingredient.jpg"/><Relationship Id="rId10" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/no_recipe_match.jpg"/><Relationship Id="rId11" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink" Target="https://github.com/Saucydotnineseven/SmartPantry" TargetMode="External"/></Relationships>'''
settings = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:settings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:updateFields w:val="true"/></w:settings>'''
core = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>Smart Pantry Manager Assignment Report Draft</dc:title><dc:creator>Mphoentle Thomas</dc:creator><dc:subject>Mobile App Development 700</dc:subject></cp:coreProperties>'''
app = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"><Application>Microsoft Word</Application></Properties>'''

with ZipFile(OUT, "w", ZIP_DEFLATED) as z:
    z.writestr("[Content_Types].xml", content_types)
    z.writestr("_rels/.rels", rels)
    z.writestr("docProps/core.xml", core)
    z.writestr("docProps/app.xml", app)
    z.writestr("word/document.xml", document)
    z.writestr("word/styles.xml", styles)
    z.writestr("word/settings.xml", settings)
    z.writestr("word/_rels/document.xml.rels", document_rels)
    for image_name in ("pantry_list.jpg", "edit_validation.jpg", "delete_confirmation.jpg", "suggested_scrambled_eggs.jpg", "settings.jpg", "add_ingredient.jpg", "edit_ingredient.jpg", "no_recipe_match.jpg"):
        z.write(Path("submission/screenshots") / image_name, f"word/media/{image_name}")

print(OUT.resolve())
