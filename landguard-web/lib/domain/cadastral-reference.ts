/**
 * Cadastral Reference domain utility.
 * Format: {COUNTRY}-{REGION}-{YEAR}-{SEQ}
 * Example: BSL-ML-2024-654321
 *
 * This module encapsulates the business rules for cadastral reference generation
 * and validation. Changes to the format should be made here only.
 */

const COUNTRY_CODE = 'BSL' // BSL = "Boussole"
const REGION_CODE = 'ML' // Mali

/**
 * Validates cadastral reference format strictly.
 * Pattern: ^[A-Z]{3}-[A-Z]{2}-\d{4}-\d{4,6}$
 */
export function isValidCadastralReference(reference: string): boolean {
    const pattern = /^[A-Z]{3}-[A-Z]{2}-\d{4}-\d{4,6}$/
    return pattern.test(reference.trim().toUpperCase())
}

/**
 * Generates a valid cadastral reference.
 * - CCN = Country (3 uppercase letters)
 * - RG = Region (2 uppercase letters)
 * - YYYY = Current year
 * - NNNNNN = 6-digit sequence (0-padded random)
 */
export function generateCadastralReference(): string {
    const currentYear = new Date().getFullYear()
    const sequenceNumber = String(Math.floor(Math.random() * 999999)).padStart(6, '0')
    return `${COUNTRY_CODE}-${REGION_CODE}-${currentYear}-${sequenceNumber}`
}

/**
 * Normalizes a cadastral reference to uppercase and trims whitespace.
 */
export function normalizeCadastralReference(reference: string): string {
    return reference.trim().toUpperCase()
}