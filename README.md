<a name="readme-top"></a>


<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/RomainLefeuvre/swhExp">
    <img src="images/logo.png" alt="Logo" width="800" >
  </a>

<h3 align="center">Open Source Android application dataset </h3>

  <p align="center">
    project_description
    <br />
    <a href="https://github.com/RomainLefeuvre/swhExp"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/RomainLefeuvre/swhExp">View Demo</a>
    ·
    <a href="https://github.com/RomainLefeuvre/swhExp/issues">Report Bug</a>
    ·
    <a href="https://github.com/RomainLefeuvre/swhExp/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->

## About The Project

The purpose of this project is to build a dataset of open source Android applications available on the Google Play
store.

To determine if a repository contains the sources of an android application a possible way is to look for the presence
of the "AndroidManifest.xml" file.

The github and gitlab api could have been used but this has some disadvantages such as a limitation in terms of api
usage or the need to adapt to the api of each forge.

Software heritage archives open source projects from many forges such as github, gitlab, bitbucket and offers api's to
access data and metadata. Using software heritage allows to have the same approach for all forges, the only api to use
is the software heritage one. If a new forge is added to software heritage, our project will automatically benefit from
it. Furthermore, it allows our experience to not depend on private forges such as github. In this project we will use
the [SWH-GRAPH](https://docs.softwareheritage.org/devel/swh-graph/index.html) java api.

SWH-GRAPH provides fast access to the graph representation of the Software Heritage Archive and is based on a compressed
representation of the SWH Merkle DAG.

## Approach

Our approach is in two parts:

1) Traverse the graph to retrieve all nodes of type origin as well as the most recent commit node of the main branch.

2) For each of these nodes, browse their children to find files named AndroidManfiest.xml

## Getting Started

ToDo

### Prerequisites

- JAVA >= 11
- Maven >= 3.6.3

### Installation

ToDo : provide a docker container

- Create a config file based on the template
- go to scripts folder  
  `cd scripts`
- Download the last full graph dataset by running  
  `sh dl_scripts.sh`
- Install locally our swh-graph fork by running
  `sh install_swh_graph_to_local_m2.sh`
- Package  
  `cd ..`  
  `mvn clean package`

<!-- USAGE EXAMPLES -->

## Usage

`java -ea -server -XX:PretenureSizeThreshold=512M -XX:MaxNewSize=4G -XX:+UseLargePages -XX:+UseTransparentHugePages -XX:+UseNUMA -XX:+UseTLAB -XX:+ResizeTLAB -Djava.io.tmpdir=../java-tmp-dir -Xmx180G -jar ./target/shTest-1.0-SNAPSHOT.jar`



<!-- ROADMAP -->

## Roadmap

- [ ] Feature 1
- [ ] Feature 2
- [ ] Feature 3
    - [ ] Nested Feature

See the [open issues](https://github.com/RomainLefeuvre/swhExp) for a full list of proposed features (and known issues).

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTRIBUTING -->

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also
simply open an issue with the tag "enhancement". Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- LICENSE -->

## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTACT -->

## Contact

Project Link: [https://github.com/RomainLefeuvre/swhExp](https://github.com/RomainLefeuvre/swhExp)

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->

[contributors-shield]: https://img.shields.io/github/contributors/RomainLefeuvre/swhExp.svg?style=for-the-badge

[contributors-url]: https://github.com/RomainLefeuvre/swhExp/graphs/contributors

[forks-shield]: https://img.shields.io/github/forks/RomainLefeuvre/swhExp.svg?style=for-the-badge

[forks-url]: https://github.com/RomainLefeuvre/swhExp/network/members

[stars-shield]: https://img.shields.io/github/stars/RomainLefeuvre/swhExp.svg?style=for-the-badge

[stars-url]: https://github.com/RomainLefeuvre/swhExp/stargazers

[issues-shield]: https://img.shields.io/github/issues/RomainLefeuvre/swhExp.svg?style=for-the-badge

[issues-url]: https://github.com/RomainLefeuvre/swhExp/issues

[license-shield]: https://img.shields.io/github/license/RomainLefeuvre/swhExp.svg?style=for-the-badge

[license-url]: https://github.com/RomainLefeuvre/swhExp/blob/master/LICENSE.txt


