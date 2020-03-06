package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.security.KeyChain;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import org.openintents.openpgp.util.OpenPgpApi;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FragmentOptionsEncryption extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Spinner spEncryptMethod;
    private SwitchCompat swSign;
    private SwitchCompat swEncrypt;
    private SwitchCompat swAutoDecrypt;

    private Spinner spOpenPgp;
    private SwitchCompat swAutocrypt;
    private SwitchCompat swAutocryptMutual;

    private Button btnManageCertificates;
    private Button btnImportKey;
    private Button btnManageKeys;
    private TextView tvKeySize;

    private List<String> openPgpProvider = new ArrayList<>();

    private final static String[] RESET_OPTIONS = new String[]{
            "default_encrypt_method", "sign_default", "encrypt_default", "auto_decrypt",
            "openpgp_provider", "autocrypt", "autocrypt_mutual"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        PackageManager pm = getContext().getPackageManager();
        View view = inflater.inflate(R.layout.fragment_options_encryption, container, false);

        // Get controls

        spEncryptMethod = view.findViewById(R.id.spEncryptMethod);
        swSign = view.findViewById(R.id.swSign);
        swEncrypt = view.findViewById(R.id.swEncrypt);
        swAutoDecrypt = view.findViewById(R.id.swAutoDecrypt);

        spOpenPgp = view.findViewById(R.id.spOpenPgp);
        swAutocrypt = view.findViewById(R.id.swAutocrypt);
        swAutocryptMutual = view.findViewById(R.id.swAutocryptMutual);

        btnManageCertificates = view.findViewById(R.id.btnManageCertificates);
        btnImportKey = view.findViewById(R.id.btnImportKey);
        btnManageKeys = view.findViewById(R.id.btnManageKeys);
        tvKeySize = view.findViewById(R.id.tvKeySize);

        Intent intent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
        List<ResolveInfo> ris = pm.queryIntentServices(intent, 0);
        for (ResolveInfo ri : ris)
            if (ri.serviceInfo != null)
                openPgpProvider.add(ri.serviceInfo.packageName);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(openPgpProvider);
        spOpenPgp.setAdapter(adapter);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        spEncryptMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1)
                    prefs.edit().putString("default_encrypt_method", "s/mime").apply();
                else
                    onNothingSelected(parent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("default_encrypt_method").apply();
            }
        });

        swSign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sign_default", checked).apply();
            }
        });

        swEncrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("encrypt_default", checked).apply();
                swSign.setEnabled(!checked);
            }
        });

        swAutoDecrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_decrypt", checked).apply();
            }
        });

        // PGP

        spOpenPgp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                prefs.edit().putString("openpgp_provider", openPgpProvider.get(position)).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("openpgp_provider").apply();
            }
        });

        swAutocrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autocrypt", checked).apply();
                swAutocryptMutual.setEnabled(checked);
            }
        });

        swAutocryptMutual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autocrypt_mutual", checked).apply();
            }
        });

        // S/MIME

        btnManageCertificates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_MANAGE_CERTIFICATES));
            }
        });

        final Intent importKey = KeyChain.createInstallIntent();
        btnImportKey.setEnabled(importKey.resolveActivity(pm) != null);
        btnImportKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(importKey);
            }
        });

        final Intent security = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        btnImportKey.setEnabled(security.resolveActivity(pm) != null);
        btnManageKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(security);
            }
        });

        try {
            int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
            tvKeySize.setText(getString(R.string.title_advanced_aes_key_size, maxKeySize));
        } catch (NoSuchAlgorithmException ex) {
            tvKeySize.setText(Log.formatThrowable(ex));
        }

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            setOptions();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default:
                onMenuDefault();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : RESET_OPTIONS)
            editor.remove(option);
        editor.apply();
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String encrypt_method = prefs.getString("default_encrypt_method", "pgp");
        if ("s/mime".equals(encrypt_method))
            spEncryptMethod.setSelection(1);

        swSign.setChecked(prefs.getBoolean("sign_default", false));
        swEncrypt.setChecked(prefs.getBoolean("encrypt_default", false));
        swSign.setEnabled(!swEncrypt.isChecked());
        swAutoDecrypt.setChecked(prefs.getBoolean("auto_decrypt", false));

        String provider = prefs.getString("openpgp_provider", "org.sufficientlysecure.keychain");
        for (int pos = 0; pos < openPgpProvider.size(); pos++)
            if (provider.equals(openPgpProvider.get(pos))) {
                spOpenPgp.setSelection(pos);
                break;
            }

        swAutocrypt.setChecked(prefs.getBoolean("autocrypt", true));
        swAutocryptMutual.setChecked(prefs.getBoolean("autocrypt_mutual", true));
        swAutocryptMutual.setEnabled(swAutocrypt.isChecked());
    }
}
