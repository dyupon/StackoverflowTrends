library(ggplot2)
library(dplyr)
library(tidyr)

read.csv("final1_quarter.csv", header = T) %>% select(-c("Q4.2019", "Q1.2020")) -> quarter1

quarter1[which(quarter1$TopicNumber == 'topic_845'),] -> 
  theme 

colSums(theme %>% select(-c("Tag", "Propensity", "TopicNumber"))) -> 
  Popularity 

theme_general <- data.frame(Popularity) 
theme_general$Time = rownames(theme_general)

every_nth = function(n) {
  return(function(x) {x[c(TRUE, rep(FALSE, n - 1))]})
}

theme_general$Time <- ordered(theme_general$Time, levels = theme_general$Time)

ggplot(theme_general, aes(x = Time, y = Popularity, group = 1)) + 
  geom_line(size = 0.7) +
  scale_x_discrete(breaks = every_nth(n = 2)) + 
  theme(axis.text.x = element_text(angle = 45)) 

times <- as.character(theme_general$Time)
theme_gathered <- pivot_longer(theme, times, "Time", values_to = "Popularity")
theme_gathered$Time <- ordered(theme_gathered$Time, levels = unique(theme_gathered$Time))
theme_gathered$Tag  <- with(theme_gathered, reorder(Tag, -Propensity))

ggplot(theme_gathered, aes(x = Time, y = Popularity, col = Tag, group = Tag)) + 
  geom_line(size = 0.7) +
  scale_x_discrete(breaks = every_nth(n = 3)) + 
  theme(axis.text.x = element_text(angle = 45)) 