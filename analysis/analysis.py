"""
Analyze the Bubble Sort performance experiment.

This script reads raw measurements from 'data/results.csv' and generates
statistical summaries and visualizations.

Outputs:
    - analysis/five_number_summary.csv: A five-number summary (min, Q1,
      median, Q3, max) of the execution time (in ms) for each condition
      (arrangement x size x algorithm).
    - figures/boxplots_<arrangement>.png: One figure per input arrangement.
      Each figure contains panels per array size showing the box plots of
      the three algorithms.

Usage:
    Run from the repository root (ensure dependencies in requirements.txt
    are installed):

    $ python analysis/analysis.py
"""
import pandas as pd
import seaborn as sns

# --- Load and prepare the data ---------------------------------------------

df = pd.read_csv("data/results.csv")

# Convert nanoseconds to milliseconds for readability
df["time_ms"] = df["time_ns"] / 1_000_000
# Shorten algorithm names (e.g. "BubbleSortPassPerItem" -> "PassPerItem")
df["algorithm"] = df["algorithm"].str.replace("BubbleSort", "")

# --- Descriptive statistics ------------------------------------------------

# One group per condition: 48 groups of 30 measurements each
groups = df.groupby(["arrangement", "size", "algorithm"])["time_ms"]
summary = groups.describe()

# Keep the count and the five-number summary
# (25% = Q1, 50% = median, 75% = Q3)
summary = summary[["count", "min", "25%", "50%", "75%", "max"]]
summary.to_csv("analysis/five_number_summary.csv")

# --- Box plots -------------------------------------------------------------

# Fixed order, so that every figure uses the same positions and colors
algorithms = ["PassPerItem", "UntilNoChange", "WhileNeeded"]
arrangements = ["SORTED", "NEARLY_SORTED", "RANDOM", "REVERSED"]

# One figure per arrangement; one panel per array size.
# Each panel has its own y-axis (sharey=False) because times differ by
# orders of magnitude across sizes; y-axes therefore do not start at zero.
for arrangement in arrangements:
    data = df[df["arrangement"] == arrangement]
    g = sns.catplot(data=data, x="algorithm", y="time_ms", col="size",
                    hue="algorithm", order=algorithms, hue_order=algorithms,
                    kind="box", sharey=False, height=3.5, aspect=0.9, legend=False)
    g.set_titles("n = {col_name}")
    g.set_axis_labels("", "Execution time (ms)")
    g.set_xticklabels(["Pass\nPerItem", "Until\nNoChange", "While\nNeeded"])
    g.figure.suptitle(f"Input: {arrangement}", y=1.04)
    g.savefig(f"figures/boxplots_{arrangement.lower()}.png", dpi=200, bbox_inches="tight")