# Scanned Contact Integration — Implementation Guide

React Native | Small Business Apps | Plugin Architecture

---

## Design Philosophy

Build one **adapter interface**, implement once per app. Adding a new integration = one new file, zero changes to core scanner code.

```
ContactCard (shared type)
     ↓
IntegrationManager  ← registers all adapters
     ↓
[HubSpotAdapter] [ZohoAdapter] [GoogleAdapter] [OutlookAdapter] [PipedriveAdapter] [AirtableAdapter] [vCardAdapter] ...
```

Every adapter gets the same `ContactCard` object and exposes the same three methods. The UI layer never knows which CRM it is talking to.

---

## Shared Adapter Interface

```ts
// src/integrations/IntegrationAdapter.ts

export interface IntegrationAdapter {
  id: string;           // unique key e.g. "hubspot"
  label: string;        // display name e.g. "HubSpot"
  icon: string;         // local asset path or URL
  isConnected(): Promise<boolean>;
  connect(): Promise<void>;   // OAuth or API key entry
  push(card: ContactCard): Promise<PushResult>;
}

export interface PushResult {
  success: boolean;
  externalId?: string;   // ID of the record created in the target app
  url?: string;          // deep link or web URL to the new record
  error?: string;
}
```

All business logic lives in the adapter. `IntegrationManager` only iterates and delegates.

---

## IntegrationManager

```ts
// src/integrations/IntegrationManager.ts
import type { IntegrationAdapter, PushResult } from './IntegrationAdapter';
import type { ContactCard } from '../types/ContactCard';

class IntegrationManager {
  private adapters: Map<string, IntegrationAdapter> = new Map();

  register(adapter: IntegrationAdapter) {
    this.adapters.set(adapter.id, adapter);
  }

  getAll(): IntegrationAdapter[] {
    return Array.from(this.adapters.values());
  }

  get(id: string): IntegrationAdapter | undefined {
    return this.adapters.get(id);
  }

  async pushToAll(card: ContactCard, ids: string[]): Promise<Record<string, PushResult>> {
    const results: Record<string, PushResult> = {};
    await Promise.allSettled(
      ids.map(async (id) => {
        const adapter = this.adapters.get(id);
        if (!adapter) return;
        try {
          results[id] = await adapter.push(card);
        } catch (err: any) {
          results[id] = { success: false, error: err.message };
        }
      })
    );
    return results;
  }
}

export const integrationManager = new IntegrationManager();
```

---

## Register Adapters in App Entry Point

```ts
// App.tsx  (or a dedicated bootstrap file)
import { integrationManager } from './src/integrations/IntegrationManager';
import { HubSpotAdapter }   from './src/integrations/adapters/HubSpotAdapter';
import { ZohoAdapter }      from './src/integrations/adapters/ZohoAdapter';
import { PipedriveAdapter } from './src/integrations/adapters/PipedriveAdapter';
import { GoogleAdapter }    from './src/integrations/adapters/GoogleAdapter';
import { OutlookAdapter }   from './src/integrations/adapters/OutlookAdapter';
import { AirtableAdapter }  from './src/integrations/adapters/AirtableAdapter';
import { VCardAdapter }     from './src/integrations/adapters/VCardAdapter';
import { WebhookAdapter }   from './src/integrations/adapters/WebhookAdapter';

integrationManager.register(new HubSpotAdapter());
integrationManager.register(new ZohoAdapter());
integrationManager.register(new PipedriveAdapter());
integrationManager.register(new GoogleAdapter());
integrationManager.register(new OutlookAdapter());
integrationManager.register(new AirtableAdapter());
integrationManager.register(new VCardAdapter());
integrationManager.register(new WebhookAdapter());
```

---

## Credential Storage

All adapters store tokens in the device secure keychain, never in AsyncStorage.

```bash
npm install react-native-keychain
```

```ts
// src/integrations/credentialStore.ts
import * as Keychain from 'react-native-keychain';

export async function saveCredential(service: string, token: string) {
  await Keychain.setGenericPassword(service, token, { service });
}

export async function getCredential(service: string): Promise<string | null> {
  const result = await Keychain.getGenericPassword({ service });
  return result ? result.password : null;
}

export async function deleteCredential(service: string) {
  await Keychain.resetGenericPassword({ service });
}
```

---

## OAuth Helper (used by HubSpot, Zoho, Google, Pipedrive)

```bash
npm install react-native-app-auth
```

```ts
// src/integrations/oauthHelper.ts
import { authorize, refresh } from 'react-native-app-auth';

export interface OAuthConfig {
  clientId: string;
  redirectUrl: string;          // must match app scheme e.g. cardsnap://oauth
  scopes: string[];
  serviceConfiguration: {
    authorizationEndpoint: string;
    tokenEndpoint: string;
  };
}

export async function oauthLogin(config: OAuthConfig): Promise<string> {
  const result = await authorize(config);
  return result.accessToken;
}
```

Add `cardsnap://oauth` to AndroidManifest.xml and Info.plist the same way as the E2E deep link (see main build doc). All OAuth adapters share this single redirect URL.

---

## Adapter Implementations

---

### 1. HubSpot

**API:** HubSpot Contacts v3 REST API  
**Auth:** OAuth 2.0  
**Free tier:** Yes, unlimited contacts on free CRM

```ts
// src/integrations/adapters/HubSpotAdapter.ts
import { saveCredential, getCredential } from '../credentialStore';
import { oauthLogin } from '../oauthHelper';
import type { IntegrationAdapter, PushResult } from '../IntegrationAdapter';
import type { ContactCard } from '../../types/ContactCard';

const OAUTH_CONFIG = {
  clientId: 'YOUR_HUBSPOT_CLIENT_ID',
  redirectUrl: 'cardsnap://oauth',
  scopes: ['crm.objects.contacts.write'],
  serviceConfiguration: {
    authorizationEndpoint: 'https://app.hubspot.com/oauth/authorize',
    tokenEndpoint: 'https://api.hubapi.com/oauth/v1/token',
  },
};

export class HubSpotAdapter implements IntegrationAdapter {
  id    = 'hubspot';
  label = 'HubSpot';
  icon  = require('../../assets/icons/hubspot.png');

  async isConnected(): Promise<boolean> {
    return !!(await getCredential('hubspot'));
  }

  async connect(): Promise<void> {
    const token = await oauthLogin(OAUTH_CONFIG);
    await saveCredential('hubspot', token);
  }

  async push(card: ContactCard): Promise<PushResult> {
    const token = await getCredential('hubspot');
    if (!token) return { success: false, error: 'Not connected' };

    const body = {
      properties: {
        firstname:  card.firstName,
        lastname:   card.lastName,
        email:      card.email,
        phone:      card.phone,
        company:    card.company,
        jobtitle:   card.title,
        website:    card.website,
        address:    card.address,
        // Custom property: source of contact
        hs_lead_source: 'Business Card Scan',
      },
    };

    const res = await fetch('https://api.hubapi.com/crm/v3/objects/contacts', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      const err = await res.json();
      return { success: false, error: err.message };
    }

    const data = await res.json();
    return {
      success: true,
      externalId: data.id,
      url: `https://app.hubspot.com/contacts/${data.id}`,
    };
  }
}
```

---

### 2. Zoho CRM

**API:** Zoho CRM v6 REST API  
**Auth:** OAuth 2.0  
**Free tier:** Up to 3 users free

```ts
// src/integrations/adapters/ZohoAdapter.ts
import { saveCredential, getCredential } from '../credentialStore';
import { oauthLogin } from '../oauthHelper';
import type { IntegrationAdapter, PushResult } from '../IntegrationAdapter';
import type { ContactCard } from '../../types/ContactCard';

const OAUTH_CONFIG = {
  clientId: 'YOUR_ZOHO_CLIENT_ID',
  redirectUrl: 'cardsnap://oauth',
  scopes: ['ZohoCRM.modules.contacts.CREATE'],
  serviceConfiguration: {
    authorizationEndpoint: 'https://accounts.zoho.com/oauth/v2/auth',
    tokenEndpoint: 'https://accounts.zoho.com/oauth/v2/token',
  },
};

export class ZohoAdapter implements IntegrationAdapter {
  id    = 'zoho';
  label = 'Zoho CRM';
  icon  = require('../../assets/icons/zoho.png');

  async isConnected() { return !!(await getCredential('zoho')); }
  async connect() {
    const token = await oauthLogin(OAUTH_CONFIG);
    await saveCredential('zoho', token);
  }

  async push(card: ContactCard): Promise<PushResult> {
    const token = await getCredential('zoho');
    if (!token) return { success: false, error: 'Not connected' };

    const body = {
      data: [{
        First_Name:   card.firstName,
        Last_Name:    card.lastName || card.name,
        Email:        card.email,
        Phone:        card.phone,
        Account_Name: card.company,
        Title:        card.title,
        Website:      card.website,
        Mailing_Street: card.address,
        Lead_Source:  'Business Card Scan',
      }],
    };

    const res = await fetch('https://www.zohoapis.com/crm/v6/Contacts', {
      method: 'POST',
      headers: {
        'Authorization': `Zoho-oauthtoken ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    const data = await res.json();
    const record = data.data?.[0];
    if (record?.code !== 'SUCCESS') {
      return { success: false, error: record?.message ?? 'Unknown error' };
    }

    return {
      success: true,
      externalId: record.details.id,
    };
  }
}
```

---

### 3. Pipedrive

**API:** Pipedrive Persons API v1  
**Auth:** API key (simpler than OAuth for small teams)  
**Free tier:** 14-day trial then paid

```ts
// src/integrations/adapters/PipedriveAdapter.ts
import { saveCredential, getCredential } from '../credentialStore';
import type { IntegrationAdapter, PushResult } from '../IntegrationAdapter';
import type { ContactCard } from '../../types/ContactCard';

export class PipedriveAdapter implements IntegrationAdapter {
  id    = 'pipedrive';
  label = 'Pipedrive';
  icon  = require('../../assets/icons/pipedrive.png');

  async isConnected() { return !!(await getCredential('pipedrive_key')); }

  // Pipedrive uses API key, not OAuth — prompt user to paste their key
  async connect(): Promise<void> {
    // Call this with the key the user enters in your settings UI
    // e.g. show a TextInput modal, then:
    //   await pipedriveAdapter.saveKey(userEnteredKey)
  }

  async saveKey(apiKey: string): Promise<void> {
    await saveCredential('pipedrive_key', apiKey);
    // Also save their company domain if using custom subdomain
  }

  async push(card: ContactCard): Promise<PushResult> {
    const apiKey = await getCredential('pipedrive_key');
    if (!apiKey) return { success: false, error: 'Not connected' };

    const body = {
      name:  card.name,
      email: card.email ? [{ value: card.email, primary: true }] : [],
      phone: card.phone ? [{ value: card.phone, primary: true }] : [],
      org_name: card.company,
      // Pipedrive custom field for job title — create this field in Pipedrive first
      // then use the field key here
    };

    const res = await fetch(
      `https://api.pipedrive.com/v1/persons?api_token=${apiKey}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      }
    );

    const data = await res.json();
    if (!data.success) return { success: false, error: data.error };

    return {
      success: true,
      externalId: String(data.data.id),
      url: `https://app.pipedrive.com/person/${data.data.id}`,
    };
  }
}
```

---

### 4. Google Contacts (People API)

**API:** Google People API v1  
**Auth:** OAuth 2.0 with Google  
**Free:** Yes, included with Google account

```ts
// src/integrations/adapters/GoogleAdapter.ts
import { saveCredential, getCredential } from '../credentialStore';
import { oauthLogin } from '../oauthHelper';
import type { IntegrationAdapter, PushResult } from '../IntegrationAdapter';
import type { ContactCard } from '../../types/ContactCard';

const OAUTH_CONFIG = {
  clientId: 'YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com',
  redirectUrl: 'cardsnap://oauth',
  scopes: ['https://www.googleapis.com/auth/contacts'],
  serviceConfiguration: {
    authorizationEndpoint: 'https://accounts.google.com/o/oauth2/v2/auth',
    tokenEndpoint: 'https://oauth2.googleapis.com/token',
  },
};

export class GoogleAdapter implements IntegrationAdapter {
  id    = 'google';
  label = 'Google Contacts';
  icon  = require('../../assets/icons/google.png');

  async isConnected() { return !!(await getCredential('google')); }
  async connect() {
    const token = await oauthLogin(OAUTH_CONFIG);
    await saveCredential('google', token);
  }

  async push(card: ContactCard): Promise<PushResult> {
    const token = await getCredential('google');
    if (!token) return { success: false, error: 'Not connected' };

    const body = {
      names: [{ givenName: card.firstName, familyName: card.lastName }],
      emailAddresses: card.email ? [{ value: card.email, type: 'work' }] : [],
      phoneNumbers:   card.phone ? [{ value: card.phone, type: 'work' }] : [],
      organizations:  card.company ? [{ name: card.company, title: card.title }] : [],
      urls:           card.website ? [{ value: card.website }] : [],
      addresses:      card.address ? [{ formattedValue: card.address, type: 'work' }] : [],
    };

    const res = await fetch(
      'https://people.googleapis.com/v1/people:createContact',
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      }
    );

    if (!res.ok) {
      const err = await res.json();
      return { success: false, error: err.error?.message };
    }

    const data = await res.json();
    return {
      success: true,
      externalId: data.resourceName,  // e.g. "people/c123456"
    };
  }
}
```

---

### 5. Microsoft Outlook (Microsoft Graph API)

**API:** Microsoft Graph v1.0 `/me/contacts`  
**Auth:** OAuth 2.0 with Microsoft  
**Free:** Yes, with any Microsoft/Office 365 account

```ts
// src/integrations/adapters/OutlookAdapter.ts
import { saveCredential, getCredential } from '../credentialStore';
import { oauthLogin } from '../oauthHelper';
import type { IntegrationAdapter, PushResult } from '../IntegrationAdapter';
import type { ContactCard } from '../../types/ContactCard';

const OAUTH_CONFIG = {
  clientId: 'YOUR_AZURE_APP_CLIENT_ID',
  redirectUrl: 'cardsnap://oauth',
  scopes: ['Contacts.ReadWrite', 'offline_access'],
  serviceConfiguration: {
    authorizationEndpoint: 'https://login.microsoftonline.com/common/oauth2/v2.0/authorize',
    tokenEndpoint: 'https://login.microsoftonline.com/common/oauth2/v2.0/token',
  },
};

export class OutlookAdapter implements IntegrationAdapter {
  id    = 'outlook';
  label = 'Outlook / Microsoft 365';
  icon  = require('../../assets/icons/outlook.png');

  async isConnected() { return !!(await getCredential('outlook')); }
  async connect() {
    const token = await oauthLogin(OAUTH_CONFIG);
    await saveCredential('outlook', token);
  }

  async push(card: ContactCard): Promise<PushResult> {
    const token = await getCredential('outlook');
    if (!token) return { success: false, error: 'Not connected' };

    const body = {
      givenName:       card.firstName,
      surname:         card.lastName,
      displayName:     card.name,
      jobTitle:        card.title,
      companyName:     card.company,
      emailAddresses:  card.email ? [{ address: card.email, name: card.name }] : [],
      businessPhones:  card.phone ? [card.phone] : [],
      businessHomePage: card.website,
      businessAddress: card.address ? { street: card.address } : undefined,
    };

    const res = await fetch('https://graph.microsoft.com/v1.0/me/contacts', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      const err = await res.json();
      return { success: false, error: err.error?.message };
    }

    const data = await res.json();
    return { success: true, externalId: data.id };
  }
}
```

---

### 6. Airtable

**API:** Airtable REST API  
**Auth:** Personal Access Token  
**Free tier:** Yes, generous free tier  
**Use case:** Lightweight CRM or lead tracker built in Airtable

```ts
// src/integrations/adapters/AirtableAdapter.ts
import { saveCredential, getCredential } from '../credentialStore';
import type { IntegrationAdapter, PushResult } from '../IntegrationAdapter';
import type { ContactCard } from '../../types/ContactCard';

// User must supply: Personal Access Token + Base ID + Table name
// Store all three in keychain as a JSON string
interface AirtableConfig { token: string; baseId: string; tableName: string; }

export class AirtableAdapter implements IntegrationAdapter {
  id    = 'airtable';
  label = 'Airtable';
  icon  = require('../../assets/icons/airtable.png');

  async isConnected() { return !!(await getCredential('airtable')); }

  async saveConfig(config: AirtableConfig) {
    await saveCredential('airtable', JSON.stringify(config));
  }

  private async getConfig(): Promise<AirtableConfig | null> {
    const raw = await getCredential('airtable');
    return raw ? JSON.parse(raw) : null;
  }

  async connect(): Promise<void> {
    // Prompt user for token + baseId + tableName in settings UI
    // then call saveConfig(...)
  }

  async push(card: ContactCard): Promise<PushResult> {
    const config = await this.getConfig();
    if (!config) return { success: false, error: 'Not connected' };

    // Field names must match your Airtable table columns exactly
    const body = {
      fields: {
        'Name':    card.name,
        'Company': card.company,
        'Title':   card.title,
        'Email':   card.email,
        'Phone':   card.phone,
        'Website': card.website,
        'Address': card.address,
        'Source':  'Business Card Scan',
        'Scanned': card.scannedAt,
      },
    };

    const res = await fetch(
      `https://api.airtable.com/v0/${config.baseId}/${encodeURIComponent(config.tableName)}`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${config.token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      }
    );

    const data = await res.json();
    if (data.error) return { success: false, error: data.error.message };

    return {
      success: true,
      externalId: data.id,
      url: `https://airtable.com/${config.baseId}`,
    };
  }
}
```

---

### 7. vCard Share (Universal Fallback)

Works with any app that accepts contacts: WhatsApp, Telegram, email, AirDrop, any CRM with a contacts import screen.

```ts
// src/integrations/adapters/VCardAdapter.ts
import vCard from 'react-native-vcards';
import RNFS from 'react-native-fs';
import Share from 'react-native-share';
import { Platform } from 'react-native';
import type { IntegrationAdapter, PushResult } from '../IntegrationAdapter';
import type { ContactCard } from '../../types/ContactCard';

export class VCardAdapter implements IntegrationAdapter {
  id    = 'vcard';
  label = 'Share as vCard';
  icon  = require('../../assets/icons/vcard.png');

  async isConnected() { return true; } // always available, no auth needed
  async connect()     { }

  async push(card: ContactCard): Promise<PushResult> {
    const vc        = vCard();
    vc.version      = '3.0';
    vc.firstName    = card.firstName || card.name;
    vc.lastName     = card.lastName;
    vc.organization = card.company;
    vc.title        = card.title;
    vc.workEmail    = card.email;
    vc.workPhone    = card.phone;
    vc.workUrl      = card.website;
    if (card.address) {
      vc.homeAddress.label  = 'Work';
      vc.homeAddress.street = card.address;
    }

    const safe    = (card.name || 'contact').replace(/[^a-zA-Z0-9]/g, '_');
    const vcfPath = `${RNFS.CachesDirectoryPath}/${safe}.vcf`;
    await RNFS.writeFile(vcfPath, vc.getFormattedString(), 'utf8');

    await Share.open({
      url:          `file://${vcfPath}`,
      type:         Platform.OS === 'android' ? 'text/x-vcard' : 'text/vcard',
      failOnCancel: false,
    });

    return { success: true };
  }
}
```

---

### 8. Webhook (Custom / Zapier / Make.com)

Sends the contact as JSON to any URL. Connects to Zapier, Make.com (formerly Integromat), n8n, or a custom backend. One webhook can fan out to hundreds of other apps.

```ts
// src/integrations/adapters/WebhookAdapter.ts
import { saveCredential, getCredential } from '../credentialStore';
import type { IntegrationAdapter, PushResult } from '../IntegrationAdapter';
import type { ContactCard } from '../../types/ContactCard';

export class WebhookAdapter implements IntegrationAdapter {
  id    = 'webhook';
  label = 'Webhook / Zapier / Make';
  icon  = require('../../assets/icons/webhook.png');

  async isConnected() { return !!(await getCredential('webhook_url')); }

  async saveUrl(url: string) {
    await saveCredential('webhook_url', url);
  }

  async connect(): Promise<void> {
    // Prompt user to paste their Zapier/Make webhook URL in settings UI
    // then call saveUrl(...)
  }

  async push(card: ContactCard): Promise<PushResult> {
    const url = await getCredential('webhook_url');
    if (!url) return { success: false, error: 'No webhook URL configured' };

    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name:      card.name,
        firstName: card.firstName,
        lastName:  card.lastName,
        company:   card.company,
        title:     card.title,
        email:     card.email,
        phone:     card.phone,
        website:   card.website,
        address:   card.address,
        scannedAt: card.scannedAt,
        source:    'business_card_scan',
      }),
    });

    return { success: res.ok, error: res.ok ? undefined : `HTTP ${res.status}` };
  }
}
```

**Zapier workflow example:** Webhook trigger → Google Sheets row → HubSpot contact → Slack notification. The card scanner app sends one POST. Zapier handles the rest.

---

## Integration Selection UI (SaveScreen addition)

After the review screen, show the list of connected integrations and let the user pick which ones to push to before saving.

```ts
// src/screens/IntegrationsScreen.tsx
import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, Switch, TouchableOpacity, StyleSheet } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import { integrationManager } from '../integrations/IntegrationManager';
import type { IntegrationAdapter } from '../integrations/IntegrationAdapter';
import type { ContactCard } from '../types/ContactCard';

export default function IntegrationsScreen() {
  const route  = useRoute<any>();
  const nav    = useNavigation<any>();
  const card: ContactCard = route.params.card;

  const [adapters, setAdapters]   = useState<IntegrationAdapter[]>([]);
  const [connected, setConnected] = useState<Record<string, boolean>>({});
  const [selected, setSelected]   = useState<Record<string, boolean>>({});
  const [pushing, setPushing]     = useState(false);

  useEffect(() => {
    const all = integrationManager.getAll();
    setAdapters(all);
    Promise.all(all.map(a => a.isConnected().then(v => [a.id, v] as [string, boolean])))
      .then(pairs => {
        const conn: Record<string, boolean> = {};
        const sel:  Record<string, boolean> = {};
        pairs.forEach(([id, v]) => { conn[id] = v; sel[id] = v; }); // default: select all connected
        setConnected(conn);
        setSelected(sel);
      });
  }, []);

  const handlePush = async () => {
    setPushing(true);
    const targets = Object.entries(selected).filter(([, v]) => v).map(([id]) => id);
    const results = await integrationManager.pushToAll(card, targets);
    setPushing(false);
    nav.navigate('PushResult', { results });
  };

  return (
    <View style={styles.container}>
      <Text style={styles.heading}>Send to...</Text>
      <FlatList
        data={adapters}
        keyExtractor={a => a.id}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <Text style={[styles.label, !connected[item.id] && styles.dimmed]}>
              {item.label}
              {!connected[item.id] && '  (not connected)'}
            </Text>
            {connected[item.id] ? (
              <Switch
                value={selected[item.id] ?? false}
                onValueChange={v => setSelected(s => ({ ...s, [item.id]: v }))}
              />
            ) : (
              <TouchableOpacity onPress={() => item.connect().then(() =>
                item.isConnected().then(v => setConnected(c => ({ ...c, [item.id]: v })))
              )}>
                <Text style={styles.connectBtn}>Connect</Text>
              </TouchableOpacity>
            )}
          </View>
        )}
      />
      <TouchableOpacity style={styles.pushBtn} onPress={handlePush} disabled={pushing}>
        <Text style={styles.pushBtnText}>{pushing ? 'Sending...' : 'Send Contact'}</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container:  { flex: 1, padding: 20, backgroundColor: '#fff' },
  heading:    { fontSize: 22, fontWeight: '700', marginBottom: 20 },
  row:        { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 14, borderBottomWidth: 1, borderColor: '#EEE' },
  label:      { fontSize: 16 },
  dimmed:     { color: '#AAA' },
  connectBtn: { color: '#007AFF', fontSize: 15 },
  pushBtn:    { backgroundColor: '#007AFF', padding: 16, borderRadius: 12, alignItems: 'center', marginTop: 24 },
  pushBtnText:{ color: '#fff', fontSize: 16, fontWeight: '600' },
});
```

---

## App Registration Setup (per integration)

| Integration | Where to register | What you get | Free tier |
|---|---|---|---|
| HubSpot | developers.hubspot.com → Create App | Client ID + Secret | Yes |
| Zoho CRM | api-console.zoho.com | Client ID + Secret | Yes (3 users) |
| Pipedrive | pipedrive.com/marketplace | Client ID or API key | 14-day trial |
| Google | console.cloud.google.com → Enable People API | OAuth Client ID | Yes |
| Microsoft | portal.azure.com → App registrations | Client ID | Yes |
| Airtable | airtable.com/account → Personal Access Tokens | Token | Yes |
| Webhook | Zapier / Make / your own server | Webhook URL | Zapier free = 100 tasks/mo |

For each OAuth app registration, add `cardsnap://oauth` as an allowed redirect URI.

---

## Adding a New Integration (3 steps)

1. Create `src/integrations/adapters/NewAppAdapter.ts` implementing `IntegrationAdapter`
2. Register it in `App.tsx`: `integrationManager.register(new NewAppAdapter())`
3. Add icon to `src/assets/icons/newapp.png`

No changes to IntegrationManager, IntegrationsScreen, or any other file.

---

## Packages Required

```json
{
  "react-native-keychain": "^8.x",
  "react-native-app-auth": "^7.x",
  "react-native-vcards": "^0.0.x",
  "react-native-fs": "^2.20.x",
  "react-native-share": "^10.x"
}
```

```bash
npm install react-native-keychain react-native-app-auth
cd ios && pod install && cd ..
```

---

## Known Issues

| Issue | Fix |
|---|---|
| Google OAuth fails on Android with redirect URI mismatch | Register the SHA-1 fingerprint of your debug keystore in Google Cloud Console |
| Microsoft OAuth returns `invalid_client` | In Azure portal, set Redirect URI type to "Mobile and desktop applications", not "Web" |
| Airtable field names case-sensitive | Column names in your Airtable base must exactly match the keys in the push body |
| Zapier webhook returns 200 even on auth failure | Check the Zap history in Zapier dashboard, not just the HTTP status |
| Token expiry not handled | Wrap all push() calls with a token refresh check; store refresh tokens in keychain alongside access tokens |
