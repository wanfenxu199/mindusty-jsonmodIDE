package com.mindustry.modgen;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class MainActivity extends Activity {

    private EditText modName, displayName, author, description, version, minGameVersion;
    private Spinner contentType;
    private Button btnGenerate, btnPack;
    private LinearLayout customFieldsContainer;
    private List<EditText[]> customFields = new ArrayList<>();

    private final int COLOR_BG = 0xFF1a1a2e;
    private final int COLOR_CARD = 0xFF2d2d44;
    private final int COLOR_ACCENT = 0xFFe67e22;
    private final int COLOR_TEXT = 0xFFe0e0e0;
    private final int COLOR_HINT = 0xFF888888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(COLOR_BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 48, 24, 48);

        TextView title = new TextView(this);
        title.setText("Mindustry 模组生成器");
        title.setTextSize(22);
        title.setTextColor(COLOR_ACCENT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 32);
        root.addView(title);

        root.addView(sectionHeader("模组基本信息"));
        LinearLayout card1 = card();
        card1.addView(label("模组名称 (name)"));
        modName = editText("my-first-mod");
        card1.addView(modName);
        card1.addView(label("显示名称 (displayName)"));
        displayName = editText("我的第一个模组");
        card1.addView(displayName);
        card1.addView(label("作者"));
        author = editText("匿名");
        card1.addView(author);
        card1.addView(label("描述"));
        description = editText("一个自动生成的模组");
        card1.addView(description);
        card1.addView(label("版本"));
        version = editText("1.0");
        card1.addView(version);
        card1.addView(label("最低游戏版本 (minGameVersion)"));
        minGameVersion = editText("146");
        card1.addView(minGameVersion);
        root.addView(card1);

        root.addView(sectionHeader("内容类型"));
        LinearLayout card2 = card();
        card2.addView(label("选择类型"));
        contentType = new Spinner(this);
		@SuppressWarnings("unchecked")
			ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
																		   new String[]{"炮塔 (ItemTurret)", "物品 (Item)", "材料 (material)", "完全自定义"});
		contentType.setAdapter(spinnerAdapter);
        contentType.setBackgroundColor(COLOR_CARD);
        card2.addView(contentType);
        root.addView(card2);

        root.addView(sectionHeader("自定义字段"));
        LinearLayout card3 = card();
        card3.setOrientation(LinearLayout.VERTICAL);

        Button btnAddField = new Button(this);
        btnAddField.setText("+ 添加自定义字段");
        btnAddField.setTextColor(Color.WHITE);
        btnAddField.setBackgroundColor(0xFF27ae60);
        btnAddField.setPadding(16, 12, 16, 12);
        btnAddField.setAllCaps(false);
        card3.addView(btnAddField);

        customFieldsContainer = new LinearLayout(this);
        customFieldsContainer.setOrientation(LinearLayout.VERTICAL);
        card3.addView(customFieldsContainer);

        btnAddField.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					addCustomField("", "");
				}
			});

        root.addView(card3);

        addCustomField("type", "ItemTurret");
        addCustomField("name", "super-turret");
        addCustomField("health", "800");

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, 24, 0, 0);

        btnGenerate = styledButton("生成模组文件", COLOR_ACCENT);
        btnPack = styledButton("打包成 ZIP", 0xFF3498db);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
        p.setMargins(8, 0, 8, 0);
        btnRow.addView(btnGenerate, p);
        btnRow.addView(btnPack, p);
        root.addView(btnRow);

        TextView footer = new TextView(this);
        footer.setText("文件保存在 内部存储/MindustryMods/");
        footer.setTextSize(12);
        footer.setTextColor(COLOR_HINT);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 32, 0, 0);
        root.addView(footer);

        scroll.addView(root);
        setContentView(scroll);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) { generateModFiles(); }
			});
        btnPack.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) { packToZip(); }
			});
    }

    private void addCustomField(String name, String value) {
        final EditText[] pair = new EditText[2];
        final LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        EditText etName = new EditText(this);
        etName.setHint("字段名");
        etName.setText(name);
        etName.setTextSize(13);
        etName.setTextColor(COLOR_TEXT);
        etName.setBackgroundColor(0xFF3a3a5c);
        row.addView(etName, new LinearLayout.LayoutParams(0, -2, 2f));

        EditText etValue = new EditText(this);
        etValue.setHint("值");
        etValue.setText(value);
        etValue.setTextSize(13);
        etValue.setTextColor(COLOR_TEXT);
        etValue.setBackgroundColor(0xFF3a3a5c);
        row.addView(etValue, new LinearLayout.LayoutParams(0, -2, 3f));

        Button btnDel = new Button(this);
        btnDel.setText("X");
        btnDel.setTextColor(Color.WHITE);
        btnDel.setBackgroundColor(0xFFc0392b);
        btnDel.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					customFieldsContainer.removeView(row);
					customFields.remove(pair);
				}
			});
        row.addView(btnDel);

        customFieldsContainer.addView(row);
        pair[0] = etName;
        pair[1] = etValue;
        customFields.add(pair);
    }

    private String buildContentJson() {
        StringBuilder sb = new StringBuilder("{\n");
        for (int i = 0; i < customFields.size(); i++) {
            String k = customFields.get(i)[0].getText().toString().trim();
            String v = customFields.get(i)[1].getText().toString().trim();
            if (k.isEmpty()) continue;
            boolean num = v.matches("-?\\d+(\\.\\d+)?");
            sb.append("  \"").append(k).append("\": ").append(num ? v : "\"" + v + "\"");
            if (i < customFields.size() - 1) sb.append(",");
            sb.append("\n");
        }
        return sb.append("}").toString();
    }

    private File modDir() {
        File d = new File(Environment.getExternalStorageDirectory(), "MindustryMods/" + modName.getText().toString().trim());
        if (!d.exists()) d.mkdirs();
        return d;
    }

    private void generateModFiles() {
        try {
            new File(Environment.getExternalStorageDirectory(), "MindustryMods").mkdirs();
            File dir = modDir();
            writeFile(new File(dir, "mod.hjson"),
					  "{\n  \"name\": \"" + modName.getText().toString().trim() + "\",\n" +
					  "  \"displayName\": \"" + displayName.getText().toString().trim() + "\",\n" +
					  "  \"author\": \"" + author.getText().toString().trim() + "\",\n" +
					  "  \"description\": \"" + description.getText().toString().trim() + "\",\n" +
					  "  \"version\": \"" + version.getText().toString().trim() + "\",\n" +
					  "  \"minGameVersion\": " + minGameVersion.getText().toString().trim() + "\n}");
            File cd = new File(dir, "content"); cd.mkdirs();
            String t = contentType.getSelectedItem().toString();
            String sub = t.contains("炮塔") ? "blocks" : (t.contains("物品") || t.contains("材料")) ? "items" : "custom";
            File td = new File(cd, sub); td.mkdirs();
            String fn = "custom-object";
            for (EditText[] p : customFields) if (p[0].getText().toString().trim().equals("name")) { fn = p[1].getText().toString().trim(); break; }
            writeFile(new File(td, fn + ".json"), buildContentJson());
            Toast.makeText(this, "模组文件已生成", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void writeFile(File f, String s) throws IOException {
        FileWriter w = new FileWriter(f); w.write(s); w.close();
    }

    private void packToZip() {
        try {
            File dir = modDir();
            File zf = new File(Environment.getExternalStorageDirectory(), "MindustryMods/" + modName.getText().toString().trim() + ".zip");
            if (zf.exists()) zf.delete();
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zf));
            zipDir(dir, dir.getName(), zos);
            zos.close();
            Toast.makeText(this, "ZIP 已保存", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "打包错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void zipDir(File d, String path, ZipOutputStream zos) throws IOException {
        for (File f : d.listFiles()) {
            if (f.isDirectory()) zipDir(f, path + "/" + f.getName(), zos);
            else {
                FileInputStream fis = new FileInputStream(f);
                zos.putNextEntry(new ZipEntry(path + "/" + f.getName()));
                byte[] buf = new byte[1024]; int n;
                while ((n = fis.read(buf)) > 0) zos.write(buf, 0, n);
                fis.close(); zos.closeEntry();
            }
        }
    }

    private TextView sectionHeader(String t) {
        TextView tv = new TextView(this); tv.setText(t); tv.setTextSize(18);
        tv.setTextColor(COLOR_ACCENT); tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(0, 24, 0, 12); return tv;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setBackgroundColor(COLOR_CARD); c.setPadding(20, 16, 20, 16);
        return c;
    }

    private TextView label(String t) {
        TextView tv = new TextView(this); tv.setText(t); tv.setTextSize(14);
        tv.setTextColor(COLOR_TEXT); tv.setPadding(0, 16, 0, 4); return tv;
    }

    private EditText editText(String h) {
        EditText et = new EditText(this); et.setHint(h); et.setText(h);
        et.setTextSize(14); et.setTextColor(COLOR_TEXT); et.setHintTextColor(COLOR_HINT);
        et.setBackgroundColor(0xFF3a3a5c); et.setPadding(16, 12, 16, 12); return et;
    }

    private Button styledButton(String t, int c) {
        Button b = new Button(this); b.setText(t); b.setTextColor(Color.WHITE);
        b.setTextSize(14); b.setBackgroundColor(c); b.setPadding(24, 16, 24, 16);
        b.setAllCaps(false); return b;
    }
}
