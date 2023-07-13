export interface CapacitorPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
