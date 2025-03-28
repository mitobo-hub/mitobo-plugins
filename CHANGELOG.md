# Change Log
All notable changes to this project will be documented in this file.

The format of the file is based on a template from [Keep a Changelog](http://keepachangelog.com/).

## [Unreleased]
### Added
### Changed
### Deprecated
### Removed
### Fixed

## [2.4] - 2025-03-14
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 2.4
### Changed
- bump to SciJava POM in version 40.0

## [2.3.1] - 2023-08-23
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 2.3.1
### Added
- Save_Image_MTB: added focus listener to register file name changes by user

## [2.3] - 2023-07-31
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 2.3
### Changed
- updated RAM specification in Maven exec goal to ensure robust execution
- updated MiToBo native lib versions
### Fixed
- fixed Archiva repository URLs

## [2.2] - 2021-05-07
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 2.2
### Fixed
- updated starters for PaCeQuant due to changed package structure

## [2.1.2] - 2021-04-09
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 2.1.2

## [2.1.1] - 2021-03-28
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 2.1.1

## [2.1] - 2020-12-01
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 2.1
### Fixed
- MTBCellCounter: fixed issues on drawing borders with only two points

## [2.0] - 2020-05-15
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 2.0
### Changed
- online Help: switched to new concept based on operator annotations

## [1.8.18] - 2020-03-27
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.18

## [1.8.17] - 2020-02-20
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.17

## [1.8.16] - 2019-12-19
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.16
### Changed
- MTBCellCounter: improved drawing of marker numbers to avoid overlapping for better readability

## [1.8.15] - 2019-07-29
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.15
### Changed
- updated mailing list address in POM file to point directly to MiToBo tag in forum.image.sc

## [1.8.14] - 2019-02-11
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.14
### Added
- MTBCellCounter: advanced options for visualization, stromules detection operator (BIOIMAGING '19)

## [1.8.13.1] - 2019-01-22
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.13.1

## [1.8.13] - 2018-12-18
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.13
### Added
- MTBCellCounter: new marker type 'line segment'
### Fixed
- MTBCellCounter: operators can now be re-run with unchanged configuration without freezing the plugin

## [1.8.12] - 2018-10-17
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.12
### Fixed
- MTBCellCounter update: fixed bug in counting plastids and GUI freezings due to event interference

## [1.8.11] - 2018-09-07
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.11
### Changed
- MTBCellCounter update: handling of detection operators now being based on operator collection, allowing to select and configure operators by double-click, fixed GUI elements on MacOS

## [1.8.10] - 2018-05-18
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.10
### Changed
- updated to MiToBo release 1.8.10 including new biofilm image analysis operators

## [1.8.9] - 2018-05-04
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.9
### Changed
- MTBCellCounter update: new plugin mechanism for easy integration of additional detectors / functionality for adding marker regions (not just centroids) / support for simultaneous analysis of multiple channels

## [1.8.8] - 2018-03-23
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.8
### Changed
- switched to MiToBo parent POM file

## [1.8.7.1] - 2018-01-31
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.7
### Fixed
- dependency to pom-scijava updated to 19.2.0

## [1.8.7] - 2018-01-31
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.7

## [1.8.6.1] - 2017-10-09
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.6.1

## [1.8.6] - 2017-09-22
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.6
### Added
- PaCeQuant plugin for pavement cell shape analysis,  
  Birgit Möller, Yvonne Poeschl, Romina Plötner, Katharina Bürstenbinder,
  "PaCeQuant: A Tool for High-Throughput Quantification of Pavement Cell Shape Characteristics",  
  Plant Physiology, Vol. 175, Issue 1, Sep 2017. DOI: https://doi.org/10.1104/pp.17.00961

## [1.8.5] - 2017-07-29
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.5

## [1.8.4] - 2017-03-27
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.4

## [1.8.3] - 2016-12-20
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.3

## [1.8.2] - 2016-11-02
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.2

## [1.8.1] - 2016-05-20
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8.1

## [1.8] - 2016-03-15
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.8

## [1.7.1] - 2016-03-04
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.7.1

## [1.7] - 2015-09-23
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released MiToBo-Plugins 1.7





