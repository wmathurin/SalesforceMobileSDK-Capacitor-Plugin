# salesforce-mobilesdk-capacitor-plugin

The Salesforce Mobile SDK Capacitor Plugin npm package allows users to interface their hybrid iOS and Android mobile applications with the Salesforce Platform, leveraging Salesforce Mobile SDK for iOS and Salesforce Mobile SDK for Android.

## Install

```bash
npm install salesforce-mobilesdk-capacitor-plugin
npx cap sync
```

## API

<docgen-index>

* [`getInfo()`](#getinfo)
* [`unregisterAppFeature(...)`](#unregisterappfeature)
* [`registerAppFeature(...)`](#registerappfeature)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getInfo()

```typescript
getInfo() => Promise<SDKInfo>
```

**Returns:** <code>Promise&lt;<a href="#sdkinfo">SDKInfo</a>&gt;</code>

--------------------


### unregisterAppFeature(...)

```typescript
unregisterAppFeature(options: { feature: string; }) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ feature: string; }</code> |

--------------------


### registerAppFeature(...)

```typescript
registerAppFeature(options: { feature: string; }) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ feature: string; }</code> |

--------------------


### Interfaces


#### SDKInfo

| Prop                        | Type                  |
| --------------------------- | --------------------- |
| **`sdkVersion`**            | <code>string</code>   |
| **`appName`**               | <code>string</code>   |
| **`appVersion`**            | <code>string</code>   |
| **`forcePluginsAvailable`** | <code>string[]</code> |
| **`bootConfig`**            | <code>string</code>   |

</docgen-api>
