import { IWallet } from "../../models/wallet.model";
import { IWalletTransaction } from "../../models/walletTransaction.model";

export interface WalletDto {
  walletId: string | null;
  balanceMinor: number;
  currency: "VND" | "USD";
}

export interface WalletTransactionDto {
  id: string;
  type: IWalletTransaction["type"];
  source: IWalletTransaction["source"];
  amount: number;
  currency: "VND" | "USD";
  balanceBefore: number;
  balanceAfter: number;
  referenceType?: IWalletTransaction["referenceType"];
  referenceId?: string | null;
  description?: string;
  createdAt: string;
}

export function mapWalletToDto(wallet: IWallet | null): WalletDto {
  if (!wallet) {
    return {
      walletId: null,
      balanceMinor: 0,
      currency: "VND",
    };
  }

  const doc = wallet as any;
  return {
    walletId: doc.id ?? doc._id?.toString() ?? null,
    balanceMinor: wallet.balanceMinor,
    currency: wallet.currency,
  };
}

export function mapWalletTransactionToDto(
  tx: IWalletTransaction
): WalletTransactionDto {
  const doc = tx as any;
  return {
    id: doc.id ?? doc._id?.toString() ?? "",
    type: tx.type,
    source: tx.source,
    amount: tx.amountMinor,
    currency: tx.currency,
    balanceBefore: tx.balanceBeforeMinor,
    balanceAfter: tx.balanceAfterMinor,
    referenceType: tx.referenceType,
    referenceId: tx.referenceId ? tx.referenceId.toString() : null,
    description: tx.description,
    createdAt: tx.createdAt.toISOString(),
  };
}
