# Analysis and Comparison of Current Research in Blockchain Security

Petit projet académique analysant et comparant trois articles scientifiques traitant de la sécurité des blockchains, sous des angles complémentaires : détection par IA, attaque de minage exploitant les preuves à divulgation nulle de connaissance (ZKP), et le résultat fondateur du *selfish mining*.


## Contenu du dépôt

| Fichier | Description |
|---|---|
| `blockchain rapport.pdf` | Rapport d'analyse complet (synthèse, comparaison, tendances, questions ouvertes) |
| `ppt blockchain.pdf` | Support de présentation du projet |
| `Generative Adversarial Networks for Cyber Threat Hunting inEthereum Blockchain.pdf` | Article 1 (source) |
| `Mining Attack with Zero Knowledge in the Blockchain.pdf` | Article 2 (source) |
| `Majority Is Not Enough- Bitcoin mining isvulnerable.pdf` | Article 3 (source) |

## Résumé

Malgré des fondations cryptographiques solides, les blockchains restent vulnérables à des attaques qui exploitent les incitations économiques, la dynamique du réseau et les comportements adverses. Ce rapport analyse trois articles scientifiques :

1. **Generative Adversarial Networks for Cyber Threat Hunting in Ethereum Blockchain** (Rabieinejad et al., 2023) — un pipeline de détection en deux phases combinant un GAN conditionnel (CTGAN) pour générer des transactions Ethereum adverses réalistes, et un réseau Bi-LSTM pour les détecter. Précision de détection rapportée : jusqu'à 99,98 %.

2. **Mining Attack with Zero Knowledge in the Blockchain** (Yu et al., ASIA CCS 2025) — introduit le *Partial Selfish Mining* (PSM) et sa version avancée (A-PSM), où l'attaquant dévoile partiellement des blocs et utilise des preuves à divulgation nulle de connaissance (ZKP) pour attirer des mineurs rationnels et augmenter sa puissance de minage effective.

3. **Majority Is Not Enough: Bitcoin Mining Is Vulnerable** (Eyal & Sirer, 2013) — l'article fondateur démontrant que le *selfish mining* peut être rentable dès 25 à 33 % de la puissance de calcul totale, remettant en cause le seuil de sécurité supposé de 50 %, et proposant une modification du protocole pour l'atténuer.

## Comparaison

- **Article 1** agit au niveau des données/transactions (détection par apprentissage automatique).
- **Articles 2 et 3** agissent au niveau du protocole et des incitations économiques (modélisation par théorie des jeux).
- Ensemble, ils montrent que la sécurité blockchain doit être pensée à plusieurs niveaux : réduire la rentabilité des déviations au niveau du protocole, tout en surveillant les comportements anormaux au niveau applicatif.

## Conclusion

La sécurité des blockchains ne repose pas uniquement sur la cryptographie : elle dépend aussi des incitations économiques, du comportement du réseau, et de la capacité d'adaptation des attaquants. Les travaux futurs devraient combiner la refonte des protocoles avec une surveillance robuste basée sur l'IA, et analyser les nouveaux outils cryptographiques (comme les ZKP) sous des modèles d'incitation réalistes.

## Références

1. E. Rabieinejad, A. Yazdinejad, R. M. Parizi, A. Dehghantanha, *Generative Adversarial Networks for Cyber Threat Hunting in Ethereum Blockchain*, Distributed Ledger Technologies: Research and Practice, 2023.
2. J. Yu, S. Gao, R. Song, Z. Cai, B. Xiao, *Mining Attack with Zero Knowledge in the Blockchain*, Proceedings of ASIA CCS, 2025.
3. I. Eyal, E. G. Sirer, *Majority Is Not Enough: Bitcoin Mining Is Vulnerable*, arXiv:1311.0243, 2013.

---

*Projet réalisé durant l'année académique 2025-2026.*

**Auteurs :** Nirmine Hiani, Hajar Chahbi, Othmane Hrimat
