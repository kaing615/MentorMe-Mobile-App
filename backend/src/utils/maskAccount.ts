export function maskAccountNumber(accountNumber: string): string {
  if (!accountNumber) return "";

  const len = accountNumber.length;
  if (len <= 4) return accountNumber;

  return "*".repeat(len - 4) + accountNumber.slice(-4);
}
